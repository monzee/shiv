package ph.codeia.shiv.processor

import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import java.util.*
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

	private enum class Encounter { ViewModelFactory, SavedStateHandle }

	@Suppress("UnstableApiUsage")
	override fun process(
		annotations: Set<TypeElement>,
		roundEnv: RoundEnvironment
	): Boolean = ProcessingContext(processingEnv).run {
		val inject = getTypeElement(INJECT)
		val fragmentElements = mutableListOf<TypeElement>()
		val vmElements = mutableListOf<TypeElement>()
		val seen = EnumSet.noneOf(Encounter::class.java)
		roundEnv.getElementsAnnotatedWith(inject)
			.asSequence()
			.onEach {
				when {
					Encounter.ViewModelFactory in seen -> {}
					it.kind != ElementKind.FIELD -> {}
					MoreElements.asVariable(it).asType().isViewModelFactory() -> {
						seen += Encounter.ViewModelFactory
					}
				}
				when {
					Encounter.SavedStateHandle in seen -> {}
					it.kind != ElementKind.FIELD -> {}
					MoreElements.asVariable(it).asType().isSavedStateHandle() -> {
						seen += Encounter.SavedStateHandle
					}
				}
			}
			.filter { it.kind == ElementKind.CONSTRUCTOR }
			.forEach {
				val classElement = MoreElements.asType(it.enclosingElement)
				when {
					classElement extends FRAGMENT ->
						fragmentElements += classElement
					classElement extends VIEW_MODEL ->
						vmElements += classElement
				}
				when {
					Encounter.ViewModelFactory in seen -> {}
					MoreTypes.asExecutable(it.asType()).parameterTypes.any { param ->
						param.isViewModelFactory()
					} -> seen += Encounter.ViewModelFactory
				}
				when {
					Encounter.SavedStateHandle in seen -> {}
					MoreTypes.asExecutable(it.asType()).parameterTypes.any { param ->
						param.isSavedStateHandle()
					} -> seen += Encounter.SavedStateHandle
				}
			}
		val shouldMultiBindVM = Encounter.ViewModelFactory in seen
		val shouldProvideSavedStateHandle = Encounter.SavedStateHandle in seen

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
				.apply {
					if (shouldProvideSavedStateHandle) {
						addField(FieldSpec
							.builder(
								String::class.java, "currentKey",
								Modifier.PRIVATE, Modifier.STATIC
							)
							.initializer(S, Names.SAVED_STATE_HANDLE_HOLDER.toString())
							.build())
						addMethods(listOf(
							MethodSpec.methodBuilder("provideSavedStateHandle")
								.addAnnotation(Names.PROVIDES)
								.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
								.addParameter(Names.ACTIVITY, "activity")
								.returns(typeNameOf(SAVED_STATE_HANDLE))
								.addStatement(
									"$1T provider = new $1T(activity)",
									Names.VIEW_MODEL_PROVIDER
								)
								.addStatement(
									"$1T holder = provider.get(currentKey, $1T.class)",
									Names.SAVED_STATE_HANDLE_HOLDER
								)
								.addStatement("return holder.handle")
								.build(),
							MethodSpec.methodBuilder("bindViewModelStoreOwner")
								.addAnnotation(Names.BINDS)
								.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
								.addParameter(Names.ACTIVITY, "activity")
								.returns(Names.VIEW_MODEL_STORE_OWNER)
								.build()
						))
					}
				}
				.addMethods(vmElements.map {
					val vmName = ClassName.get(it)
					MethodSpec.methodBuilder("provide${it.simpleName}")
						.addAnnotation(Names.PROVIDES)
						.addAnnotation(Names.SHARED)
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.addParameter(Names.VIEW_MODEL_STORE_OWNER, "owner")
						.addParameter(
							ParameterizedTypeName.get(Names.PROVIDER, vmName),
							"provider"
						)
						.returns(vmName)
						.apply {
							if (shouldProvideSavedStateHandle) {
								addStatement(
									"currentKey = \"$1L#$2L\"",
									vmName.toString(),
									Names.SAVED_STATE_HANDLE_HOLDER.simpleName()
								)
							}
						}
						.addStatement(
							"return $T.createViewModel(owner, provider, $T.class)",
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