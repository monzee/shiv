package ph.codeia.shiv.processor

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types


/*
 * This file is a part of the Shiv project.
 */

const val SIGNATURE = "Generated by Shiv (https://github.com/monzee/shiv)"
const val INJECT = "javax.inject.Inject"
const val FRAGMENT = "androidx.fragment.app.Fragment"
const val VIEW_MODEL = "androidx.lifecycle.ViewModel"
const val VIEW_MODEL_FACTORY = "androidx.lifecycle.ViewModelProvider.Factory"
const val VIEW_MODEL_STORE_OWNER = "androidx.lifecycle.ViewModelStoreOwner"
const val INJECTING_VIEW_MODEL_FACTORY = "ph.codeia.shiv.InjectingViewModelFactory"
const val T = "${'$'}T"

object Names {
	val MODULE = ClassName.get("dagger", "Module")
	val BINDS = ClassName.get("dagger", "Binds")
	val PROVIDES = ClassName.get("dagger", "Provides")
	val INTO_MAP = ClassName.get("dagger.multibindings", "IntoMap")
	val CLASS_KEY = ClassName.get("dagger.multibindings", "ClassKey")
	val PROVIDER = ClassName.get("javax.inject", "Provider")
	val SHARED = ClassName.get("ph.codeia.shiv", "Shared")
	val SHIV = ClassName.get("ph.codeia.shiv", "Shiv")
}

@AutoService(Processor::class)
class ShivProcessor : AbstractProcessor() {
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
							AnnotationSpec
								.builder(Names.CLASS_KEY)
								.addMember("value", "$T.class", fragmentName)
								.build()
						)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(fragmentName, "fragment")
						.returns(TypeName(FRAGMENT))
						.build()
				})
				.build()
				.let { JavaFile.builder("shiv", it) }
				.addFileComment(SIGNATURE)
				.build()
				.writeTo(filer)
		}

		if (vmElements.isNotEmpty()) {
			TypeSpec.classBuilder("SharedViewModelProviders")
				.addAnnotation(Names.MODULE)
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addMethods(vmElements.map {
					val vmName = ClassName.get(it)
					MethodSpec.methodBuilder("provide${it.simpleName}")
						.addAnnotation(Names.PROVIDES)
						.addAnnotation(Names.SHARED)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.addParameter(TypeName(VIEW_MODEL_STORE_OWNER), "owner")
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
							.returns(TypeName(VIEW_MODEL))
							.build()
					})
					.build()
					.let { JavaFile.builder("shiv", it) }
					.addFileComment(SIGNATURE)
					.build()
					.writeTo(filer)
			}
		}

		false
	}
}

class ProcessingContext(processingEnv: ProcessingEnvironment) :
	Elements by processingEnv.elementUtils
	, Types by processingEnv.typeUtils
	, Messager by processingEnv.messager {
	val filer = processingEnv.filer

	infix fun Element.extends(superType: String): Boolean = let {
		it extends getTypeElement(superType)
	}

	infix fun Element.extends(superType: Element): Boolean = let {
		it.asType() extends superType.asType()
	}

	infix fun TypeMirror.extends(superType: String): Boolean = let {
		it extends getTypeElement(superType).asType()
	}

	infix fun TypeMirror.extends(superType: TypeMirror): Boolean = let {
		isSubtype(it, superType)
	}

	fun TypeName(fqcn: String): TypeName = TypeName.get(getTypeElement(fqcn).asType())

	fun TypeMirror.isViewModelFactory(): Boolean = let {
		it extends VIEW_MODEL_FACTORY || it extends INJECTING_VIEW_MODEL_FACTORY
	}
}