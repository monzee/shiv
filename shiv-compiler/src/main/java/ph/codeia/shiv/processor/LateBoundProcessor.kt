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
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/*
 * This file is a part of the Shiv project.
 */


@AutoService(Processor::class)
class LateBoundProcessor : AbstractProcessor() {
	private data class PartialClass(
		val constructor: ExecutableElement,
		val classDef: TypeElement,
		val constructorParams: List<Pair<Int, ParameterSpec>>,
		val bindParams: List<Pair<Int, ParameterSpec>>
	) {
		val constructorArgs: List<String>
			get() = run {
				val provided = constructorParams.map { it.toExpr(true) }
				val asIs = bindParams.map { it.toExpr(false) }
				(provided + asIs)
					.sortedBy { it.first }
					.map { it.second }
			}

		private fun Pair<Int, ParameterSpec>.toExpr(
			isProvider: Boolean
		): Pair<Int, String> = let { (index, param) ->
			val expr =
				if (isProvider) "${param.name}.get()"
				else param.name
			index to expr
		}
	}

	@Suppress("UnstableApiUsage", "DefaultLocale")
	override fun process(
		annotations: Set<TypeElement>,
		roundEnv: RoundEnvironment
	): Boolean = ProcessingContext(processingEnv).run {
		val lateBoundAnnotation = getTypeElement(LATE_BOUND)
		val lateBoundType = getDeclaredType(lateBoundAnnotation)
		val seen = mutableSetOf<CharSequence>()
		val compare = MoreTypes.equivalence()
		roundEnv.getElementsAnnotatedWith(lateBoundAnnotation)
			.asSequence()
			.filter { it.enclosingElement.kind == ElementKind.CONSTRUCTOR }
			.filterNot { Modifier.PRIVATE in it.enclosingElement.modifiers }
			.map {
				val consElem = MoreElements.asExecutable(it.enclosingElement)
				val classElem = MoreElements.asType(consElem.enclosingElement)
				consElem to classElem
			}
			.filterNot { (_, classElem) -> Modifier.ABSTRACT in classElem.modifiers }
			.filterNot { (_, classElem) -> classElem.qualifiedName in seen }
			.map { (consElem, classElem) ->
				seen += classElem.qualifiedName
				val (lateBound, injectable) = consElem.parameters
					.mapIndexed(::Pair)
					.partition { (_, param) ->
						param.annotationMirrors.any {
							compare.equivalent(
								it.annotationType,
								lateBoundType
							)
						}
					}

				PartialClass(
					consElem,
					classElem,
					injectable.map { (index, elem) ->
						val param = MoreElements.asVariable(elem)
						val paramType = TypeName.get(elem.asType()).box()
						val providerType = ParameterizedTypeName.get(Names.PROVIDER, paramType)
						index to ParameterSpec
							.builder(providerType, param.simpleName.toString())
							.addAnnotations(param.annotationMirrors.map(AnnotationSpec::get))
							.build()
					},
					lateBound.map { (index, elem) ->
						index to ParameterSpec.get(elem)
					}
				)
			}
			.map { partial ->
				val consParams = partial.constructorParams.map { it.second }
				val fields = consParams.map {
					FieldSpec.builder(it.type, it.name)
						.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
						.build()
				}
				val consBody = CodeBlock.builder()
					.also { builder ->
						consParams.forEach {
							builder.addStatement("this.$1N = $1N", it.name)
						}
					}
					.build()
				val bindParams = partial.bindParams.map { it.second }
				val consArgs = partial.constructorArgs.joinToString(", ")
				val targetName = ClassName.get(partial.classDef)
				val bindBody = CodeBlock.builder()
					.addStatement("return new $1T($consArgs)", targetName)
					.build()
				val packageName = getPackageOf(partial.classDef).qualifiedName
				val classModifiers = (partial.classDef.modifiers - Modifier.STATIC + Modifier.FINAL)
					.toTypedArray()

				packageName to TypeSpec.classBuilder("Partial${targetName.simpleName()}")
					.addModifiers(*classModifiers)
					.addFields(fields)
					.addMethod(MethodSpec.constructorBuilder()
						.addAnnotation(Names.INJECT)
						.addModifiers(partial.constructor.modifiers)
						.addParameters(consParams)
						.addCode(consBody)
						.build())
					.addMethod(MethodSpec.methodBuilder("bind")
						.addModifiers(Modifier.PUBLIC)
						.addParameters(bindParams)
						.addCode(bindBody)
						.returns(targetName)
						.build())
					.addOriginatingElement(partial.constructor)
					.build()
			}
			.map { (packageName, classSpec) ->
				JavaFile.builder(packageName.toString(), classSpec)
					.addFileComment(SIGNATURE)
					.skipJavaLangImports(true)
					.build()
			}
			.forEach {
				it.writeTo(filer)
			}
		true
	}

	override fun getSupportedSourceVersion(): SourceVersion = run {
		SourceVersion.latestSupported()
	}

	override fun getSupportedAnnotationTypes(): Set<String> = run {
		setOf(LATE_BOUND)
	}
}
