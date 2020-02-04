package ph.codeia.shiv.processor

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/*
 * This file is a part of the Shiv project.
 */


@AutoService(Processor::class)
class InjectProcessor : AbstractProcessor() {
	override fun getSupportedSourceVersion(): SourceVersion = run {
		SourceVersion.latestSupported()
	}

	override fun getSupportedAnnotationTypes(): Set<String> = run {
		setOf(INJECT)
	}

	@Suppress("UnstableApiUsage")
	override fun process(
		annotations: Set<TypeElement>,
		roundEnv: RoundEnvironment
	): Boolean = ProcessingContext(processingEnv).run {
		val inject = getTypeElement(INJECT)
		val fragmentElements = roundEnv.getElementsAnnotatedWith(inject)
			.filter { it.kind == ElementKind.CONSTRUCTOR }
			.filter { it.enclosingElement extends FRAGMENT }
			.map { MoreElements.asType(it.enclosingElement) }
		val vmElements = roundEnv.getElementsAnnotatedWith(inject)
			.filter { it.kind == ElementKind.CONSTRUCTOR }
			.filter { it.enclosingElement extends VIEW_MODEL }
			.map { MoreElements.asType(it.enclosingElement) }
		val shouldMultiBindVM = roundEnv.getElementsAnnotatedWith(inject)
			.any {
				when (it.kind) {
					ElementKind.FIELD -> MoreElements.asVariable(it)
						.asType()
						.isViewModelFactory()
					ElementKind.CONSTRUCTOR -> MoreTypes.asExecutable(it.asType())
						.parameterTypes
						.any { param -> param.isViewModelFactory() }
					else -> false
				}
			}

		if (fragmentElements.isNotEmpty()) {
			TypeSpec.interfaceBuilder("FragmentBindings")
				.addAnnotation(Names.MODULE)
				.addModifiers(Modifier.PUBLIC)
				.addMethods(fragmentElements.map {
					val fragmentName = ClassName.get(it)
					MethodSpec.methodBuilder("bind${it.simpleName}")
						.addAnnotation(Names.BINDS)
						.addAnnotation(Names.INTO_MAP)
						.addAnnotation(
							AnnotationSpec.builder(Names.STRING_KEY)
								.addMember("value", S, it.qualifiedName)
								.build()
						)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(fragmentName, "fragment")
						.returns(typeNameOf(FRAGMENT))
						.build()
				})
				.build()
				.let { JavaFile.builder("shiv", it) }
				.addFileComment(SIGNATURE)
				.skipJavaLangImports(true)
				.build()
				.writeTo(filer)
		}

		if (vmElements.isNotEmpty()) {
			TypeSpec.classBuilder("SharedViewModelProviders")
				.addAnnotation(Names.MODULE)
				.addModifiers(
					Modifier.PUBLIC,
					Modifier.ABSTRACT
				)
				.addMethods(vmElements.map {
					val vmName = ClassName.get(it)
					MethodSpec.methodBuilder("provide${it.simpleName}")
						.addAnnotation(Names.PROVIDES)
						.addAnnotation(Names.SHARED)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.addParameter(typeNameOf(VIEW_MODEL_STORE_OWNER), "owner")
						.addParameter(
							ParameterizedTypeName.get(Names.PROVIDER, vmName),
							"provider"
						)
						.returns(vmName)
						.addCode(
							"return $T.createViewModel(owner, provider, $T.class);\n",
							Names.SHIV,
							vmName
						)
						.build()
				})
				.build()
				.let { JavaFile.builder("shiv", it) }
				.addFileComment(SIGNATURE)
				.skipJavaLangImports(true)
				.build()
				.writeTo(filer)

			if (shouldMultiBindVM) {
				TypeSpec.interfaceBuilder("ViewModelBindings")
					.addAnnotation(Names.MODULE)
					.addModifiers(Modifier.PUBLIC)
					.addMethods(vmElements.map {
						val vmName = ClassName.get(it)
						MethodSpec.methodBuilder("bind${it.simpleName}")
							.addAnnotation(Names.BINDS)
							.addAnnotation(Names.INTO_MAP)
							.addAnnotation(
								AnnotationSpec.builder(Names.CLASS_KEY)
									.addMember("value", "$T.class", vmName)
									.build()
							)
							.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.addParameter(vmName, "vm")
							.returns(typeNameOf(VIEW_MODEL))
							.build()
					})
					.build()
					.let { JavaFile.builder("shiv", it) }
					.addFileComment(SIGNATURE)
					.skipJavaLangImports(true)
					.build()
					.writeTo(filer)
			}
		}

		false
	}
}