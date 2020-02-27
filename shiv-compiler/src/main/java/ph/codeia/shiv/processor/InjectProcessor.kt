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

	private class UniqueNames {
		val taken = mutableSetOf<String>()

		operator fun get(base: String): String = run {
			var key = base
			var n = 1
			while (key in taken) {
				key = "${base}_$n"
				n += 1
			}
			taken += key
			key
		}
	}

	@Suppress("UnstableApiUsage")
	override fun process(
		annotations: Set<TypeElement>,
		roundEnv: RoundEnvironment
	): Boolean = ProcessingContext(processingEnv).run {
		val inject = getTypeElement(INJECT)
		val fragmentElements = mutableListOf<TypeElement>()
		val vmElements = mutableListOf<TypeElement>()
		val needsSavedState = mutableSetOf<TypeElement>()
		val names = UniqueNames()
		var shouldMultiBindVM = false
		roundEnv.getElementsAnnotatedWith(inject)
			.asSequence()
			.filter { it.kind == ElementKind.CONSTRUCTOR }
			.forEach {
				val classElement = MoreElements.asType(it.enclosingElement)
				when {
					classElement extends FRAGMENT ->
						fragmentElements += classElement
					classElement extends VIEW_MODEL -> {
						vmElements += classElement
						MoreTypes.asExecutable(it.asType())
							.parameterTypes
							.any { param -> param extends SAVED_STATE_HANDLE }
							.let { found ->
								if (found) needsSavedState += classElement
							}
					}
				}
				when {
					shouldMultiBindVM -> {}
					MoreTypes.asExecutable(it.asType()).parameterTypes.any { param ->
						param extends VIEW_MODEL_FACTORY
					} -> shouldMultiBindVM = true
				}
			}
		val shouldProvideSavedStateHandle = needsSavedState.isNotEmpty()

		if (fragmentElements.isNotEmpty()) {
			TypeSpec.interfaceBuilder("FragmentBindings")
				.addAnnotation(Names.MODULE)
				.addModifiers(Modifier.PUBLIC)
				.addMethods(fragmentElements.map {
					val fragmentName = ClassName.get(it)
					MethodSpec.methodBuilder(names["bind${it.simpleName}"])
						.addAnnotation(Names.BINDS)
						.addAnnotation(Names.INTO_MAP)
						.addAnnotation(
							AnnotationSpec.builder(Names.STRING_KEY)
								.addMember("value", "$1S", it.qualifiedName)
								.build()
						)
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(fragmentName, "fragment")
						.returns(typeNameOf(FRAGMENT))
						.build()
				})
				.apply {
					fragmentElements.forEach { addOriginatingElement(it) }
				}
				.build()
				.let { JavaFile.builder("shiv", it) }
				.addFileComment(SIGNATURE)
				.skipJavaLangImports(true)
				.build()
				.writeTo(filer)
		}

		if (vmElements.isNotEmpty()) {
			TypeSpec.classBuilder("SharedViewModelProviders")
				.addAnnotation(AnnotationSpec.builder(Names.MODULE).run {
					if (shouldProvideSavedStateHandle) addMember(
						"includes",
						"$1T.class",
						Names.SAVED_STATE_MODULE
					)
					build()
				})
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addMethods(vmElements.map {
					val vmName = ClassName.get(it)
					MethodSpec.methodBuilder(names["provide${it.simpleName}"])
						.addAnnotation(Names.PROVIDES)
						.addAnnotation(Names.SHARED)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.addParameter(Names.VIEW_MODEL_STORE_OWNER, "owner")
						.addParameter(
							ParameterizedTypeName.get(Names.PROVIDER, vmName),
							"provider"
						)
						.apply {
							if (it in needsSavedState) {
								addParameter(Names.SAVED_STATE_HOLDER_KEY, "key")
								addStatement(
									"key.set(\"$1L:$2L\")",
									vmName.toString(),
									Names.SAVED_STATE_HOLDER.simpleName()
								)
							}
						}
						.returns(vmName)
						.addStatement(
							"return $1T.createViewModel(owner, provider, $2T.class)",
							Names.SHIV,
							vmName
						)
						.build()
				})
				.apply {
					vmElements.forEach { addOriginatingElement(it) }
				}
				.build()
				.let { JavaFile.builder("shiv", it) }
				.addFileComment(SIGNATURE)
				.skipJavaLangImports(true)
				.build()
				.writeTo(filer)

			if (shouldMultiBindVM) {
				TypeSpec.interfaceBuilder("ViewModelBindings")
					.addAnnotation(AnnotationSpec.builder(Names.MODULE).run {
						if (shouldProvideSavedStateHandle) addMember(
							"includes",
							"$1T.class",
							Names.SAVED_STATE_MODULE
						)
						build()
					})
					.addModifiers(Modifier.PUBLIC)
					.addMethods(vmElements.map {
						val vmName = ClassName.get(it)
						MethodSpec.methodBuilder(names["bind${it.simpleName}"])
							.addAnnotation(Names.BINDS)
							.addAnnotation(Names.INTO_MAP)
							.addAnnotation(
								AnnotationSpec.builder(Names.CLASS_KEY)
									.addMember("value", "$1T.class", vmName)
									.build()
							)
							.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
							.addParameter(vmName, "vm")
							.returns(typeNameOf(VIEW_MODEL))
							.build()
					})
					.apply {
						vmElements.forEach { addOriginatingElement(it) }
					}
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