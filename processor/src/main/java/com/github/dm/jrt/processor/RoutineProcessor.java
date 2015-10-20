/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dm.jrt.processor;

import com.github.dm.jrt.annotation.Alias;
import com.github.dm.jrt.annotation.CoreInstances;
import com.github.dm.jrt.annotation.Input;
import com.github.dm.jrt.annotation.Input.InputMode;
import com.github.dm.jrt.annotation.InputMaxSize;
import com.github.dm.jrt.annotation.InputOrder;
import com.github.dm.jrt.annotation.InputTimeout;
import com.github.dm.jrt.annotation.Inputs;
import com.github.dm.jrt.annotation.Invoke;
import com.github.dm.jrt.annotation.Invoke.InvocationMode;
import com.github.dm.jrt.annotation.MaxInstances;
import com.github.dm.jrt.annotation.Output;
import com.github.dm.jrt.annotation.Output.OutputMode;
import com.github.dm.jrt.annotation.OutputMaxSize;
import com.github.dm.jrt.annotation.OutputOrder;
import com.github.dm.jrt.annotation.OutputTimeout;
import com.github.dm.jrt.annotation.Priority;
import com.github.dm.jrt.annotation.ReadTimeout;
import com.github.dm.jrt.annotation.ReadTimeoutAction;
import com.github.dm.jrt.annotation.SharedFields;
import com.github.dm.jrt.builder.InvocationConfiguration.OrderType;
import com.github.dm.jrt.builder.InvocationConfiguration.TimeoutActionType;
import com.github.dm.jrt.channel.InvocationChannel;
import com.github.dm.jrt.channel.OutputChannel;
import com.github.dm.jrt.routine.Routine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Annotation processor used to generate proxy classes enabling method asynchronous invocations.
 * <p/>
 * Created by davide-maestroni on 11/03/2014.
 */
public class RoutineProcessor extends AbstractProcessor {

    protected static final String NEW_LINE = System.getProperty("line.separator");

    private static final boolean DEBUG = false;

    private final byte[] mByteBuffer = new byte[2048];

    private final HashMap<String, TypeMirror> mPrimitiveMirrors = new HashMap<String, TypeMirror>();

    protected TypeMirror invocationChannelType;

    protected TypeMirror iterableType;

    protected TypeMirror listType;

    protected TypeMirror objectType;

    protected TypeMirror outputChannelType;

    protected TypeMirror routineType;

    private String mFooter;

    private String mHeader;

    private String mMethodArray;

    private String mMethodArrayInvocation;

    private String mMethodArrayInvocationCollection;

    private String mMethodArrayInvocationVoid;

    private String mMethodAsync;

    private String mMethodElementArray;

    private String mMethodElementAsync;

    private String mMethodElementList;

    private String mMethodElementResult;

    private String mMethodElementVoid;

    private String mMethodHeader;

    private String mMethodInputsChannel;

    private String mMethodInputsRoutine;

    private String mMethodInvocation;

    private String mMethodInvocationCollection;

    private String mMethodInvocationFooter;

    private String mMethodInvocationHeader;

    private String mMethodInvocationVoid;

    private String mMethodList;

    private String mMethodResult;

    private String mMethodVoid;

    /**
     * Prints the stacktrace of the specified throwable into a string.
     *
     * @param throwable the throwable instance.
     * @return the printed stacktrace.
     */
    @NotNull
    protected static String printStackTrace(@NotNull final Throwable throwable) {

        final StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        return Collections.singleton("com.github.dm.jrt.proxy.annotation.Proxy");
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {

        // Let's return the latest version
        final SourceVersion[] values = SourceVersion.values();
        return values[values.length - 1];
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {

        super.init(processingEnv);
        routineType = getMirrorFromName(Routine.class.getCanonicalName());
        invocationChannelType = getMirrorFromName(InvocationChannel.class.getCanonicalName());
        outputChannelType = getMirrorFromName(OutputChannel.class.getCanonicalName());
        iterableType = getMirrorFromName(Iterable.class.getCanonicalName());
        listType = getMirrorFromName(List.class.getCanonicalName());
        objectType = getMirrorFromName(Object.class.getCanonicalName());
        final Types typeUtils = processingEnv.getTypeUtils();
        final HashMap<String, TypeMirror> primitiveMirrors = mPrimitiveMirrors;
        primitiveMirrors.put(char.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.CHAR));
        primitiveMirrors.put(byte.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.BYTE));
        primitiveMirrors.put(boolean.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.BOOLEAN));
        primitiveMirrors.put(short.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.SHORT));
        primitiveMirrors.put(int.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.INT));
        primitiveMirrors.put(long.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.LONG));
        primitiveMirrors.put(float.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.FLOAT));
        primitiveMirrors.put(double.class.getCanonicalName(),
                             typeUtils.getPrimitiveType(TypeKind.DOUBLE));
        primitiveMirrors.put(char[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.CHAR)));
        primitiveMirrors.put(byte[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.BYTE)));
        primitiveMirrors.put(boolean[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.BOOLEAN)));
        primitiveMirrors.put(short[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.SHORT)));
        primitiveMirrors.put(int[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.INT)));
        primitiveMirrors.put(long[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.LONG)));
        primitiveMirrors.put(float[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.FLOAT)));
        primitiveMirrors.put(double[].class.getCanonicalName(),
                             typeUtils.getArrayType(typeUtils.getPrimitiveType(TypeKind.DOUBLE)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean process(final Set<? extends TypeElement> typeElements,
            final RoundEnvironment roundEnvironment) {

        if (roundEnvironment.processingOver()) {

            return false;
        }

        final Types typeUtils = processingEnv.getTypeUtils();

        for (final TypeElement annotationElement : getSupportedAnnotationElements()) {

            final TypeMirror annotationType = annotationElement.asType();

            for (final Element element : ElementFilter.typesIn(
                    roundEnvironment.getElementsAnnotatedWith(annotationElement))) {

                if (element.getKind() != ElementKind.INTERFACE) {

                    processingEnv.getMessager()
                                 .printMessage(Kind.ERROR,
                                               "Annotated element is not an interface: " + element);
                    throw new RuntimeException("Annotated element is not an interface: " + element);
                }

                final TypeElement classElement = (TypeElement) element;
                final List<ExecutableElement> methodElements =
                        ElementFilter.methodsIn(element.getEnclosedElements());

                for (final TypeMirror typeMirror : classElement.getInterfaces()) {

                    final Element superElement = typeUtils.asElement(typeMirror);

                    if (superElement != null) {

                        mergeParentMethods(methodElements, ElementFilter.methodsIn(
                                superElement.getEnclosedElements()));
                    }
                }

                final Object targetElement = getAnnotationValue(element, annotationType, "value");

                if (targetElement != null) {

                    createProxy(annotationElement, classElement,
                                typeUtils.asElement(getMirrorFromName(targetElement.toString())),
                                methodElements);
                }
            }
        }

        return false;
    }

    /**
     * Builds the string used to replace "${paramValues}" in the template.
     *
     * @param targetMethodElement the target method element.
     * @return the string.
     */
    @NotNull
    protected String buildCollectionParamValues(
            @NotNull final ExecutableElement targetMethodElement) {

        final VariableElement targetParameter = targetMethodElement.getParameters().get(0);
        return "(" + targetParameter.asType() + ") objects";
    }

    /**
     * Builds the string used to replace "${genericTypes}" in the template.
     *
     * @param element the annotated element.
     * @return the string.
     */
    @NotNull
    protected String buildGenericTypes(@NotNull final TypeElement element) {

        final List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();

        if (typeParameters.isEmpty()) {

            return "";
        }

        final StringBuilder builder = new StringBuilder("<");

        for (final TypeParameterElement typeParameterElement : typeParameters) {

            if (builder.length() > 1) {

                builder.append(", ");
            }

            builder.append(typeParameterElement.asType());
        }

        return builder.append(">").toString();
    }

    /**
     * Builds the string used to replace "${inputOptions}" in the template.
     *
     * @param methodElement the method element.
     * @param inputMode     the input mode.
     * @return the string.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String buildInputOptions(final ExecutableElement methodElement,
            final InputMode inputMode) {

        return ((inputMode == InputMode.CHANNEL) || (inputMode == InputMode.COLLECTION))
                ? ".orderByCall()" : "";
    }

    /**
     * Builds the string used to replace "${inputParams}" in the template.
     *
     * @param methodElement the method element.
     * @return the string.
     */
    @NotNull
    protected String buildInputParams(@NotNull final ExecutableElement methodElement) {

        final Types typeUtils = processingEnv.getTypeUtils();
        final TypeMirror outputChannelType = this.outputChannelType;
        final StringBuilder builder = new StringBuilder();

        for (final VariableElement variableElement : methodElement.getParameters()) {

            builder.append(".pass(");

            if (typeUtils.isAssignable(outputChannelType,
                                       typeUtils.erasure(variableElement.asType())) && (
                    variableElement.getAnnotation(Input.class) != null)) {

                builder.append("(").append(typeUtils.erasure(outputChannelType)).append("<?>)");

            } else {

                builder.append("(Object)");
            }

            builder.append(variableElement).append(")");
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${paramValues}" in the template.
     *
     * @param targetMethodElement the target method element.
     * @return the string.
     */
    @NotNull
    protected String buildParamValues(@NotNull final ExecutableElement targetMethodElement) {

        int count = 0;
        final StringBuilder builder = new StringBuilder();

        for (final VariableElement variableElement : targetMethodElement.getParameters()) {

            if (builder.length() > 0) {

                builder.append(", ");
            }

            builder.append("(")
                   .append(getBoxedType(variableElement.asType()))
                   .append(") objects.get(")
                   .append(count++)
                   .append(")");
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${paramVars}" in the template.
     *
     * @param methodElement the method element.
     * @return the string.
     */
    @NotNull
    protected String buildParamVars(@NotNull final ExecutableElement methodElement) {

        final StringBuilder builder = new StringBuilder();

        for (final VariableElement variableElement : methodElement.getParameters()) {

            if (builder.length() > 0) {

                builder.append(", ");
            }

            builder.append("final ")
                   .append(variableElement.asType())
                   .append(" ")
                   .append(variableElement);
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${params}" in the template.
     *
     * @param methodElement the method element.
     * @return the string.
     */
    @NotNull
    protected CharSequence buildParams(@NotNull final ExecutableElement methodElement) {

        final StringBuilder builder = new StringBuilder();

        for (final VariableElement variableElement : methodElement.getParameters()) {

            if (builder.length() > 0) {

                builder.append(", ");
            }

            builder.append(variableElement);
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${routineFieldsInit}" in the template.
     *
     * @param size the number of method elements.
     * @return the string.
     */
    @NotNull
    protected String buildRoutineFieldsInit(final int size) {

        final StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= size; ++i) {

            builder.append("mRoutine")
                   .append(i)
                   .append(" = ")
                   .append("initRoutine")
                   .append(i)
                   .append("(target, invocationConfiguration, proxyConfiguration);")
                   .append(NEW_LINE);
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${routineBuilderOptions}" in the template.
     *
     * @param methodElement the method element.
     * @return the string.
     */
    @NotNull
    protected String buildRoutineOptions(@NotNull final ExecutableElement methodElement) {

        final StringBuilder builder = new StringBuilder();
        final CoreInstances coreInstancesAnnotation =
                methodElement.getAnnotation(CoreInstances.class);

        if (coreInstancesAnnotation != null) {

            builder.append(".withCoreInstances(")
                   .append(coreInstancesAnnotation.value())
                   .append(")");
        }

        final InputMaxSize inputSizeAnnotation = methodElement.getAnnotation(InputMaxSize.class);

        if (inputSizeAnnotation != null) {

            builder.append(".withInputMaxSize(").append(inputSizeAnnotation.value()).append(")");
        }

        final InputOrder inputOrderAnnotation = methodElement.getAnnotation(InputOrder.class);

        if (inputOrderAnnotation != null) {

            builder.append(".withInputOrder(")
                   .append(OrderType.class.getCanonicalName())
                   .append(".")
                   .append(inputOrderAnnotation.value())
                   .append(")");
        }

        final InputTimeout inputTimeoutAnnotation = methodElement.getAnnotation(InputTimeout.class);

        if (inputTimeoutAnnotation != null) {

            builder.append(".withInputTimeout(")
                   .append(inputTimeoutAnnotation.value())
                   .append(", ")
                   .append(TimeUnit.class.getCanonicalName())
                   .append(".")
                   .append(inputTimeoutAnnotation.unit())
                   .append(")");
        }

        final MaxInstances maxInstancesAnnotation = methodElement.getAnnotation(MaxInstances.class);

        if (maxInstancesAnnotation != null) {

            builder.append(".withMaxInstances(").append(maxInstancesAnnotation.value()).append(")");
        }

        final OutputMaxSize outputSizeAnnotation = methodElement.getAnnotation(OutputMaxSize.class);

        if (outputSizeAnnotation != null) {

            builder.append(".withOutputMaxSize(").append(outputSizeAnnotation.value()).append(")");
        }

        final OutputOrder outputOrderAnnotation = methodElement.getAnnotation(OutputOrder.class);

        if (outputOrderAnnotation != null) {

            builder.append(".withOutputOrder(")
                   .append(OrderType.class.getCanonicalName())
                   .append(".")
                   .append(outputOrderAnnotation.value())
                   .append(")");
        }

        final OutputTimeout outputTimeoutAnnotation =
                methodElement.getAnnotation(OutputTimeout.class);

        if (outputTimeoutAnnotation != null) {

            builder.append(".withOutputTimeout(")
                   .append(outputTimeoutAnnotation.value())
                   .append(", ")
                   .append(TimeUnit.class.getCanonicalName())
                   .append(".")
                   .append(outputTimeoutAnnotation.unit())
                   .append(")");
        }

        final Priority priorityAnnotation = methodElement.getAnnotation(Priority.class);

        if (priorityAnnotation != null) {

            builder.append(".withPriority(").append(priorityAnnotation.value()).append(")");
        }

        final ReadTimeout readTimeoutAnnotation = methodElement.getAnnotation(ReadTimeout.class);

        if (readTimeoutAnnotation != null) {

            builder.append(".withReadTimeout(")
                   .append(readTimeoutAnnotation.value())
                   .append(", ")
                   .append(TimeUnit.class.getCanonicalName())
                   .append(".")
                   .append(readTimeoutAnnotation.unit())
                   .append(")");
        }

        final ReadTimeoutAction actionAnnotation =
                methodElement.getAnnotation(ReadTimeoutAction.class);

        if (actionAnnotation != null) {

            builder.append(".withReadTimeoutAction(")
                   .append(TimeoutActionType.class.getCanonicalName())
                   .append(".")
                   .append(actionAnnotation.value())
                   .append(")");
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${resultRawSizedArray}" in the template.
     *
     * @param returnType the target method return type.
     * @return the string.
     */
    @NotNull
    protected String buildSizedArray(@NotNull final TypeMirror returnType) {

        final StringBuilder builder = new StringBuilder();

        if (returnType.getKind() == TypeKind.ARRAY) {

            final String typeString = returnType.toString();
            final int firstBracket = typeString.indexOf('[');
            builder.append(typeString.substring(0, firstBracket))
                   .append("[$$size]")
                   .append(typeString.substring(firstBracket));

        } else {

            builder.append(returnType).append("[$$size]");
        }

        return builder.toString();
    }

    /**
     * Builds the string used to replace "${targetMethodParamTypes}" in the template.
     *
     * @param targetMethodElement the target method element.
     * @return the string.
     */
    @NotNull
    protected String buildTargetParamTypes(final ExecutableElement targetMethodElement) {

        final Types typeUtils = processingEnv.getTypeUtils();
        final StringBuilder builder = new StringBuilder();

        for (final VariableElement variableElement : targetMethodElement.getParameters()) {

            builder.append(", ")
                   .append(typeUtils.erasure(variableElement.asType()))
                   .append(".class");
        }

        return builder.toString();
    }

    /**
     * Returns the element value of the specified annotation attribute.
     *
     * @param element        the annotated element.
     * @param annotationType the annotation type.
     * @param attributeName  the attribute name.
     * @return the value.
     */
    @Nullable
    protected Object getAnnotationValue(@NotNull final Element element,
            @NotNull final TypeMirror annotationType, @NotNull final String attributeName) {

        AnnotationValue value = null;

        for (final AnnotationMirror mirror : element.getAnnotationMirrors()) {

            if (!mirror.getAnnotationType().equals(annotationType)) {

                continue;
            }

            final Set<? extends Entry<? extends ExecutableElement, ? extends AnnotationValue>> set =
                    mirror.getElementValues().entrySet();

            for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : set) {

                if (attributeName.equals(entry.getKey().getSimpleName().toString())) {

                    value = entry.getValue();
                    break;
                }
            }
        }

        return (value != null) ? value.getValue() : null;
    }

    /**
     * Returned the boxed type.
     *
     * @param type the type to be boxed.
     * @return the boxed type.
     */
    protected TypeMirror getBoxedType(@Nullable final TypeMirror type) {

        if ((type != null) && (type.getKind().isPrimitive() || (type.getKind() == TypeKind.VOID))) {

            return processingEnv.getTypeUtils().boxedClass((PrimitiveType) type).asType();
        }

        return type;
    }

    /**
     * Gets the default class name prefix.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the name prefix.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getDefaultClassPrefix(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        String defaultPrefix = null;

        for (final Element valueElement : annotationElement.getEnclosedElements()) {

            if (valueElement.getSimpleName().toString().equals("DEFAULT_CLASS_PREFIX")) {

                defaultPrefix = (String) ((VariableElement) valueElement).getConstantValue();
                break;
            }
        }

        return (defaultPrefix == null) ? "" : defaultPrefix;
    }

    /**
     * Gets the default class name suffix.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the name suffix.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getDefaultClassSuffix(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        String defaultSuffix = null;

        for (final Element valueElement : annotationElement.getEnclosedElements()) {

            if (valueElement.getSimpleName().toString().equals("DEFAULT_CLASS_SUFFIX")) {

                defaultSuffix = (String) ((VariableElement) valueElement).getConstantValue();
                break;
            }
        }

        return (defaultSuffix == null) ? "" : defaultSuffix;
    }

    /**
     * Returns the specified template as a string.
     *
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    protected String getFooterTemplate() throws IOException {

        if (mFooter == null) {

            mFooter = parseTemplate("/templates/footer.txt");
        }

        return mFooter;
    }

    /**
     * Returns the name of the generated class.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the class name.
     */
    @NotNull
    @SuppressWarnings({"ConstantConditions", "UnusedParameters"})
    protected String getGeneratedClassName(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        String className =
                (String) getAnnotationValue(element, annotationElement.asType(), "className");

        if ((className == null) || className.equals("*")) {

            className = element.getSimpleName().toString();
            Element enclosingElement = element.getEnclosingElement();

            while ((enclosingElement != null) && !(enclosingElement instanceof PackageElement)) {

                className = enclosingElement.getSimpleName().toString() + "_" + className;
                enclosingElement = enclosingElement.getEnclosingElement();
            }
        }

        return className;
    }

    /**
     * Returns the package of the generated class.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the class package.
     */
    @NotNull
    @SuppressWarnings({"ConstantConditions", "UnusedParameters"})
    protected String getGeneratedClassPackage(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        String classPackage =
                (String) getAnnotationValue(element, annotationElement.asType(), "classPackage");

        if ((classPackage == null) || classPackage.equals("*")) {

            classPackage = getPackage(element).getQualifiedName().toString();
        }

        return classPackage;
    }

    /**
     * Returns the prefix of the generated class name.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the name prefix.
     */
    @NotNull
    @SuppressWarnings({"ConstantConditions", "UnusedParameters"})
    protected String getGeneratedClassPrefix(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        final String classPrefix =
                (String) getAnnotationValue(element, annotationElement.asType(), "classPrefix");
        return (classPrefix == null) ? getDefaultClassPrefix(annotationElement, element,
                                                             targetElement) : classPrefix;
    }

    /**
     * Returns the suffix of the generated class name.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the name suffix.
     */
    @NotNull
    @SuppressWarnings({"ConstantConditions", "UnusedParameters"})
    protected String getGeneratedClassSuffix(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        final String classSuffix =
                (String) getAnnotationValue(element, annotationElement.asType(), "classSuffix");
        return (classSuffix == null) ? getDefaultClassSuffix(annotationElement, element,
                                                             targetElement) : classSuffix;
    }

    /**
     * Returns the specified template as a string.
     *
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    protected String getHeaderTemplate() throws IOException {

        if (mHeader == null) {

            mHeader = parseTemplate("/templates/header.txt");
        }

        return mHeader;
    }

    /**
     * Gets the input transfer mode.
     *
     * @param methodElement   the method element.
     * @param annotation      the parameter annotation.
     * @param targetParameter the target parameter.
     * @param length          the total number of parameters.
     * @return the input mode.
     */
    @NotNull
    protected InputMode getInputMode(@NotNull final ExecutableElement methodElement,
            @NotNull final Input annotation, @NotNull final VariableElement targetParameter,
            final int length) {

        final Types typeUtils = processingEnv.getTypeUtils();
        final TypeMirror outputChannelType = this.outputChannelType;
        final TypeMirror targetType = targetParameter.asType();
        final TypeMirror targetTypeErasure = typeUtils.erasure(targetType);
        final Element annotationElement =
                typeUtils.asElement(getMirrorFromName(Input.class.getCanonicalName()));
        final TypeMirror annotationType = annotationElement.asType();
        final TypeMirror targetMirror =
                (TypeMirror) getAnnotationValue(targetParameter, annotationType, "value");
        InputMode inputMode = annotation.mode();

        if (inputMode == InputMode.CHANNEL) {

            if (!typeUtils.isAssignable(targetTypeErasure, outputChannelType)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async input with mode " + InputMode.CHANNEL
                                + " must implement an " + outputChannelType);
            }

        } else if (inputMode == InputMode.COLLECTION) {

            if (!typeUtils.isAssignable(targetTypeErasure, outputChannelType)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async input with mode " + InputMode.COLLECTION
                                + " must implement an " + outputChannelType);
            }

            if ((targetMirror != null) && (targetMirror.getKind() != TypeKind.ARRAY)
                    && !typeUtils.isAssignable(listType, targetMirror)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async input with mode " + InputMode.COLLECTION
                                + " must be bound to an array or a superclass of " + listType);
            }

            if (length > 1) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async input with mode " + InputMode.COLLECTION
                                + " cannot be applied to a method taking " + length
                                + " input parameters");
            }

        } else { // InputMode.ELEMENT

            if ((targetType.getKind() != TypeKind.ARRAY) && !typeUtils.isAssignable(
                    targetTypeErasure, iterableType)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async input with mode " + InputMode.ELEMENT
                                + " must be an array or implement an " + iterableType);
            }

            if ((targetType.getKind() == TypeKind.ARRAY) && !typeUtils.isAssignable(
                    getBoxedType(((ArrayType) targetType).getComponentType()),
                    getBoxedType(targetMirror))) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] the async input array with mode " + InputMode.ELEMENT
                                + " does not match the bound type: " + targetMirror);
            }

            if (length > 1) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async input with mode " + InputMode.ELEMENT
                                + " cannot be applied to a method taking " + length
                                + " input parameters");
            }
        }

        return inputMode;
    }

    /**
     * Gets the routine invocation mode.
     *
     * @param methodElement the method element.
     * @param annotation    the method annotation.
     * @return the invocation mode.
     */
    @NotNull
    protected InvocationMode getInvocationMode(@NotNull final ExecutableElement methodElement,
            @NotNull final Invoke annotation) {

        final InvocationMode invocationMode = annotation.value();

        if ((invocationMode == InvocationMode.PARALLEL) && (methodElement.getParameters().size()
                > 1)) {

            throw new IllegalArgumentException(
                    "methods annotated with invocation mode " + InvocationMode.PARALLEL
                            + " must have at maximum one input parameter: " + methodElement);
        }

        return invocationMode;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodArrayInvocationCollectionTemplate(
            @NotNull final ExecutableElement methodElement, final int count) throws IOException {

        if (mMethodArrayInvocationCollection == null) {

            mMethodArrayInvocationCollection =
                    parseTemplate("/templates/method_array_invocation_collection.txt");
        }

        return mMethodArrayInvocationCollection;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodArrayInvocationTemplate(
            @NotNull final ExecutableElement methodElement, final int count) throws IOException {

        if (mMethodArrayInvocation == null) {

            mMethodArrayInvocation = parseTemplate("/templates/method_array_invocation.txt");
        }

        return mMethodArrayInvocation;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodArrayInvocationVoidTemplate(
            @NotNull final ExecutableElement methodElement, final int count) throws IOException {

        if (mMethodArrayInvocationVoid == null) {

            mMethodArrayInvocationVoid =
                    parseTemplate("/templates/method_array_invocation_void.txt");
        }

        return mMethodArrayInvocationVoid;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodArrayTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodArray == null) {

            mMethodArray = parseTemplate("/templates/method_array.txt");
        }

        return mMethodArray;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodAsyncTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodAsync == null) {

            mMethodAsync = parseTemplate("/templates/method_async.txt");
        }

        return mMethodAsync;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodElementArrayTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodElementArray == null) {

            mMethodElementArray = parseTemplate("/templates/method_element_array.txt");
        }

        return mMethodElementArray;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodElementAsyncTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodElementAsync == null) {

            mMethodElementAsync = parseTemplate("/templates/method_element_async.txt");
        }

        return mMethodElementAsync;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodElementListTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodElementList == null) {

            mMethodElementList = parseTemplate("/templates/method_element_list.txt");
        }

        return mMethodElementList;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodElementResultTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodElementResult == null) {

            mMethodElementResult = parseTemplate("/templates/method_element_result.txt");
        }

        return mMethodElementResult;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodElementVoidTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodElementVoid == null) {

            mMethodElementVoid = parseTemplate("/templates/method_element_void.txt");
        }

        return mMethodElementVoid;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodHeaderTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodHeader == null) {

            mMethodHeader = parseTemplate("/templates/method_header.txt");
        }

        return mMethodHeader;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInputsChannelTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodInputsChannel == null) {

            mMethodInputsChannel = parseTemplate("/templates/method_inputs_channel.txt");
        }

        return mMethodInputsChannel;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInputsRoutineTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodInputsRoutine == null) {

            mMethodInputsRoutine = parseTemplate("/templates/method_inputs_routine.txt");
        }

        return mMethodInputsRoutine;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInvocationCollectionTemplate(
            @NotNull final ExecutableElement methodElement, final int count) throws IOException {

        if (mMethodInvocationCollection == null) {

            mMethodInvocationCollection =
                    parseTemplate("/templates/method_invocation_collection.txt");
        }

        return mMethodInvocationCollection;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInvocationFooterTemplate(
            @NotNull final ExecutableElement methodElement, final int count) throws IOException {

        if (mMethodInvocationFooter == null) {

            mMethodInvocationFooter = parseTemplate("/templates/method_invocation_footer.txt");
        }

        return mMethodInvocationFooter;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInvocationHeaderTemplate(
            @NotNull final ExecutableElement methodElement, final int count) throws IOException {

        if (mMethodInvocationHeader == null) {

            mMethodInvocationHeader = parseTemplate("/templates/method_invocation_header.txt");
        }

        return mMethodInvocationHeader;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInvocationTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodInvocation == null) {

            mMethodInvocation = parseTemplate("/templates/method_invocation.txt");
        }

        return mMethodInvocation;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodInvocationVoidTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodInvocationVoid == null) {

            mMethodInvocationVoid = parseTemplate("/templates/method_invocation_void.txt");
        }

        return mMethodInvocationVoid;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodListTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodList == null) {

            mMethodList = parseTemplate("/templates/method_list.txt");
        }

        return mMethodList;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodResultTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodResult == null) {

            mMethodResult = parseTemplate("/templates/method_result.txt");
        }

        return mMethodResult;
    }

    /**
     * Returns the specified template as a string.
     *
     * @param methodElement the method element.
     * @param count         the method count.
     * @return the template.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getMethodVoidTemplate(@NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        if (mMethodVoid == null) {

            mMethodVoid = parseTemplate("/templates/method_void.txt");
        }

        return mMethodVoid;
    }

    /**
     * Gets the element with the specified name.
     *
     * @param typeName the type name.
     * @return the element.
     */
    protected TypeMirror getMirrorFromName(@NotNull final String typeName) {

        final String name = normalizeTypeName(typeName);
        final TypeMirror typeMirror = mPrimitiveMirrors.get(name);

        if (typeMirror != null) {

            return typeMirror;
        }

        final TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(name);
        return (typeElement != null) ? typeElement.asType() : null;
    }

    /**
     * Gets the return output transfer mode.
     *
     * @param methodElement       the method element.
     * @param targetMethodElement the target method element.
     * @return the output mode.
     */
    @NotNull
    protected OutputMode getOutputMode(@NotNull final ExecutableElement methodElement,
            @NotNull final ExecutableElement targetMethodElement) {

        final Types typeUtils = processingEnv.getTypeUtils();
        final TypeMirror outputChannelType = this.outputChannelType;
        final TypeMirror returnType = methodElement.getReturnType();
        final TypeMirror erasure = typeUtils.erasure(returnType);
        final TypeMirror targetMirror = typeUtils.erasure(targetMethodElement.getReturnType());
        OutputMode outputMode = methodElement.getAnnotation(Output.class).value();

        if (outputMode == OutputMode.CHANNEL) {

            if (!typeUtils.isAssignable(outputChannelType, erasure)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async output with mode " + OutputMode.CHANNEL
                                + " must be a superclass of " + outputChannelType);
            }

        } else if (outputMode == OutputMode.ELEMENT) {

            if (!typeUtils.isAssignable(outputChannelType, erasure)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async output with mode " + OutputMode.CHANNEL
                                + " must be a superclass of " + outputChannelType);
            }

            if ((targetMirror != null) && (targetMirror.getKind() != TypeKind.ARRAY)
                    && !typeUtils.isAssignable(targetMirror, iterableType)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async output with mode " + OutputMode.ELEMENT
                                + " must be bound to an array or a type implementing an "
                                + iterableType);
            }

        } else { // OutputMode.COLLECTION

            if ((returnType.getKind() != TypeKind.ARRAY) && !typeUtils.isAssignable(listType,
                                                                                    erasure)) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] an async output with mode " + OutputMode.COLLECTION
                                + " must be an array or a superclass of " + listType);
            }

            if ((returnType.getKind() == TypeKind.ARRAY) && !typeUtils.isAssignable(
                    getBoxedType(targetMirror),
                    getBoxedType(((ArrayType) returnType).getComponentType()))) {

                throw new IllegalArgumentException(
                        "[" + methodElement.getEnclosingElement() + "." + methodElement
                                + "] the async output array with mode " + OutputMode.COLLECTION
                                + " does not match the bound type: " + targetMirror);
            }
        }

        return outputMode;
    }

    /**
     * Returns the package of the specified element..
     *
     * @param element the element.
     * @return the package.
     */
    @NotNull
    protected PackageElement getPackage(@NotNull final TypeElement element) {

        return processingEnv.getElementUtils().getPackageOf(element);
    }

    /**
     * Gets the generated source name.
     *
     * @param annotationElement the annotation element.
     * @param element           the annotated element.
     * @param targetElement     the target element.
     * @return the source name.
     */
    @NotNull
    @SuppressWarnings("UnusedParameters")
    protected String getSourceName(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement) {

        final StringBuilder builder = new StringBuilder(
                getGeneratedClassPackage(annotationElement, element, targetElement));

        if (builder.length() > 0) {

            builder.append('.');
        }

        return builder.append(getGeneratedClassPrefix(annotationElement, element, targetElement))
                      .append(getGeneratedClassName(annotationElement, element, targetElement))
                      .append(getGeneratedClassSuffix(annotationElement, element, targetElement))
                      .toString();
    }

    /**
     * Returns the list of supported annotation elements.
     *
     * @return the elements of the supported annotations.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    protected List<TypeElement> getSupportedAnnotationElements() {

        final Types typeUtils = processingEnv.getTypeUtils();
        final ArrayList<TypeElement> elements = new ArrayList<TypeElement>();

        for (final String name : getSupportedAnnotationTypes()) {

            elements.add((TypeElement) typeUtils.asElement(getMirrorFromName(name)));
        }

        return elements;
    }

    /**
     * Checks if the specified methods have assignable parameters, that is, all the parameters of
     * the first method can be assigned to the ones of the second in the same order.
     *
     * @param firstMethodElement  the first method element.
     * @param secondMethodElement the second method element.
     * @return whether the methods have the same parameters.
     */
    protected boolean haveAssignableParameters(@NotNull final ExecutableElement firstMethodElement,
            @NotNull final ExecutableElement secondMethodElement) {

        final List<? extends VariableElement> firstTypeParameters =
                firstMethodElement.getParameters();
        final List<? extends VariableElement> secondTypeParameters =
                secondMethodElement.getParameters();
        final int length = firstTypeParameters.size();

        if (length != secondTypeParameters.size()) {

            return false;
        }

        final Types typeUtils = processingEnv.getTypeUtils();

        for (int i = 0; i < length; ++i) {

            final TypeMirror firstType = firstTypeParameters.get(i).asType();
            final TypeMirror secondType = secondTypeParameters.get(i).asType();

            if (!typeUtils.isAssignable(typeUtils.erasure(firstType),
                                        typeUtils.erasure(secondType))) {

                return false;
            }
        }

        return true;
    }

    /**
     * Returns the normalized version of the specified type name.
     *
     * @param typeName the type name.
     * @return the normalized type name.
     */
    @NotNull
    protected String normalizeTypeName(@NotNull final String typeName) {

        if (typeName.endsWith(".class")) {

            return typeName.substring(0, typeName.length() - ".class".length());
        }

        return typeName;
    }

    /**
     * Parses the template resource with the specified path and returns it into a string.
     *
     * @param path the resource path.
     * @return the template as a string.
     * @throws java.io.IOException if an I/O error occurred.
     */
    @NotNull
    @SuppressFBWarnings(value = "UI_INHERITANCE_UNSAFE_GETRESOURCE",
            justification = "inheritance of the processor class must be supported")
    protected String parseTemplate(@NotNull final String path) throws IOException {

        final byte[] buffer = mByteBuffer;
        final InputStream resourceStream = getClass().getResourceAsStream(path);

        try {

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int length;

            while ((length = resourceStream.read(buffer)) > 0) {

                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            return outputStream.toString("UTF-8");

        } finally {

            try {

                resourceStream.close();

            } catch (final IOException ignored) {

            }
        }
    }

    @SuppressWarnings("PointlessBooleanExpression")
    private void createProxy(@NotNull final TypeElement annotationElement,
            @NotNull final TypeElement element, @NotNull final Element targetElement,
            @NotNull final List<ExecutableElement> methodElements) {

        Writer writer = null;

        try {

            final Filer filer = processingEnv.getFiler();

            if (!DEBUG) {

                final JavaFileObject sourceFile = filer.createSourceFile(
                        getSourceName(annotationElement, element, targetElement));
                writer = sourceFile.openWriter();

            } else {

                writer = new StringWriter();
            }

            String header;
            final Types typeUtils = processingEnv.getTypeUtils();
            final String packageName =
                    getGeneratedClassPackage(annotationElement, element, targetElement);
            header = getHeaderTemplate().replace("${generatedPackage}",
                                                 (packageName.length() > 0) ? "package "
                                                         + packageName + ";" : "");
            header = header.replace("${generatedClassName}",
                                    getGeneratedClassPrefix(annotationElement, element,
                                                            targetElement) +
                                            getGeneratedClassName(annotationElement, element,
                                                                  targetElement) +
                                            getGeneratedClassSuffix(annotationElement, element,
                                                                    targetElement));
            header = header.replace("${genericTypes}", buildGenericTypes(element));
            header = header.replace("${classFullName}", targetElement.asType().toString());
            header = header.replace("${interfaceFullName}", element.asType().toString());
            header = header.replace("${classErasure}",
                                    typeUtils.erasure(targetElement.asType()).toString());
            header = header.replace("${interfaceErasure}",
                                    typeUtils.erasure(element.asType()).toString());
            header = header.replace("${routineFieldsInit}",
                                    buildRoutineFieldsInit(methodElements.size()));
            writer.append(header);
            int count = 0;

            for (final ExecutableElement methodElement : methodElements) {

                ++count;
                writeMethod(writer, element, targetElement, methodElement, count);
            }

            writer.append(getFooterTemplate());

        } catch (final IOException e) {

            processingEnv.getMessager()
                         .printMessage(Kind.ERROR,
                                       "IOException while writing template; " + printStackTrace(e));
            throw new RuntimeException(e);

        } finally {

            if (writer != null) {

                try {

                    writer.close();

                } catch (final IOException ignored) {

                }
            }
        }

        if (DEBUG) {

            System.out.println(writer.toString());
        }
    }

    @NotNull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    private ExecutableElement findMatchingMethod(@NotNull final ExecutableElement methodElement,
            @NotNull final Element targetElement) {

        String methodName = methodElement.getSimpleName().toString();
        ExecutableElement targetMethod = null;
        final Alias asyncAnnotation = methodElement.getAnnotation(Alias.class);

        if (asyncAnnotation != null) {

            methodName = asyncAnnotation.value();

            for (final ExecutableElement targetMethodElement : ElementFilter.methodsIn(
                    targetElement.getEnclosedElements())) {

                final Alias targetAsyncAnnotation = targetMethodElement.getAnnotation(Alias.class);

                if ((targetAsyncAnnotation != null) && methodName.equals(
                        targetAsyncAnnotation.value())) {

                    targetMethod = targetMethodElement;

                    break;
                }
            }
        }

        final Types typeUtils = processingEnv.getTypeUtils();
        final TypeMirror inputAnnotationType = getMirrorFromName(Input.class.getCanonicalName());
        final TypeMirror inputsAnnotationType = getMirrorFromName(Inputs.class.getCanonicalName());

        if (targetMethod == null) {

            if (methodElement.getAnnotation(Inputs.class) != null) {

                final List<?> annotationParams =
                        (List<?>) getAnnotationValue(methodElement, inputsAnnotationType, "value");

                if (annotationParams != null) {

                    final int length = annotationParams.size();

                    for (final ExecutableElement targetMethodElement : ElementFilter.methodsIn(
                            targetElement.getEnclosedElements())) {

                        if (!methodName.equals(targetMethodElement.getSimpleName().toString())) {

                            continue;
                        }

                        final List<? extends VariableElement> typeParameterElements =
                                targetMethodElement.getParameters();

                        if (length == typeParameterElements.size()) {

                            boolean matches = true;

                            for (int i = 0; i < length; ++i) {

                                final TypeMirror paramMirror =
                                        getMirrorFromName(annotationParams.get(i).toString());
                                final TypeMirror typeParameterMirror =
                                        typeParameterElements.get(i).asType();

                                if (!typeUtils.isSameType(typeUtils.erasure(paramMirror),
                                                          typeUtils.erasure(typeParameterMirror))) {

                                    matches = false;
                                    break;
                                }
                            }

                            if (matches) {

                                targetMethod = targetMethodElement;
                                break;
                            }
                        }
                    }
                }

            } else {

                final List<? extends VariableElement> interfaceTypeParameters =
                        methodElement.getParameters();
                final int length = interfaceTypeParameters.size();

                for (final ExecutableElement targetMethodElement : ElementFilter.methodsIn(
                        targetElement.getEnclosedElements())) {

                    if (!methodName.equals(targetMethodElement.getSimpleName().toString())) {

                        continue;
                    }

                    final List<? extends VariableElement> typeParameterElements =
                            targetMethodElement.getParameters();

                    if (length == typeParameterElements.size()) {

                        boolean matches = true;

                        for (int i = 0; i < length; ++i) {

                            Object value = null;
                            final VariableElement variableElement = interfaceTypeParameters.get(i);

                            if (variableElement.getAnnotation(Input.class) != null) {

                                value = getAnnotationValue(variableElement, inputAnnotationType,
                                                           "value");
                            }

                            final TypeMirror typeParameterMirror =
                                    typeParameterElements.get(i).asType();

                            if (!typeUtils.isSameType(typeUtils.erasure(
                                                              (value != null) ? (TypeMirror) value
                                                                      : variableElement.asType()),
                                                      typeUtils.erasure(typeParameterMirror))) {

                                matches = false;
                                break;
                            }
                        }

                        if (matches) {

                            targetMethod = targetMethodElement;
                            break;
                        }
                    }
                }
            }

        } else if (methodElement.getAnnotation(Inputs.class) != null) {

            final List<?> annotationParams =
                    (List<?>) getAnnotationValue(methodElement, inputsAnnotationType, "value");

            if (annotationParams != null) {

                final int length = annotationParams.size();
                final List<? extends VariableElement> typeParameterElements =
                        targetMethod.getParameters();

                if (length == typeParameterElements.size()) {

                    for (int i = 0; i < length; ++i) {

                        if (getMirrorFromName(annotationParams.get(i).toString()) == null) {

                            throw new NullPointerException(
                                    annotationParams.get(i).toString() + " - "
                                            + typeUtils.asElement(
                                            typeUtils.getPrimitiveType(TypeKind.CHAR)));
                        }

                        final TypeMirror paramMirror =
                                getMirrorFromName(annotationParams.get(i).toString());
                        final TypeMirror typeParameterMirror =
                                typeParameterElements.get(i).asType();

                        if (!typeUtils.isSameType(typeUtils.erasure(paramMirror),
                                                  typeUtils.erasure(typeParameterMirror))) {

                            targetMethod = null;
                            break;
                        }
                    }

                } else {

                    targetMethod = null;
                }

            } else {

                targetMethod = null;
            }

        } else {

            final List<? extends VariableElement> interfaceTypeParameters =
                    methodElement.getParameters();
            final int length = interfaceTypeParameters.size();
            final List<? extends VariableElement> typeParameterElements =
                    targetMethod.getParameters();

            if (length == typeParameterElements.size()) {

                for (int i = 0; i < length; ++i) {

                    Object value = null;
                    final VariableElement variableElement = interfaceTypeParameters.get(i);

                    if (variableElement.getAnnotation(Input.class) != null) {

                        value = getAnnotationValue(variableElement, inputAnnotationType, "value");
                    }

                    final TypeMirror typeParameterMirror = typeParameterElements.get(i).asType();

                    if (!typeUtils.isSameType(typeUtils.erasure((value != null) ? (TypeMirror) value
                                                                        : variableElement.asType()),
                                              typeUtils.erasure(typeParameterMirror))) {

                        targetMethod = null;
                        break;
                    }
                }

            } else {

                targetMethod = null;
            }
        }

        if (targetMethod == null) {

            throw new IllegalArgumentException(
                    "[" + methodElement + "] cannot find matching method in target class");
        }

        return targetMethod;
    }

    private void mergeParentMethods(@NotNull final List<ExecutableElement> methods,
            @NotNull final List<ExecutableElement> parentMethods) {

        final Types typeUtils = processingEnv.getTypeUtils();

        for (final ExecutableElement parentMethod : parentMethods) {

            boolean isOverride = false;

            for (final ExecutableElement method : methods) {

                if (parentMethod.getSimpleName().equals(method.getSimpleName())
                        && typeUtils.isAssignable(typeUtils.erasure(method.getReturnType()),
                                                  typeUtils.erasure(
                                                          parentMethod.getReturnType()))) {

                    if (haveAssignableParameters(method, parentMethod)) {

                        isOverride = true;
                        break;
                    }
                }
            }

            if (!isOverride) {

                methods.add(parentMethod);
            }
        }
    }

    private void writeMethod(@NotNull final Writer writer, @NotNull final TypeElement element,
            @NotNull final Element targetElement, @NotNull final ExecutableElement methodElement,
            final int count) throws IOException {

        final Types typeUtils = processingEnv.getTypeUtils();
        final TypeMirror outputChannelType = this.outputChannelType;
        final TypeMirror listType = this.listType;
        final TypeMirror objectType = this.objectType;
        final ExecutableElement targetMethod = findMatchingMethod(methodElement, targetElement);
        TypeMirror targetReturnType = targetMethod.getReturnType();
        final boolean isVoid = (targetReturnType.getKind() == TypeKind.VOID);
        final Invoke invocationAnnotation = methodElement.getAnnotation(Invoke.class);
        final Inputs inputsAnnotation = methodElement.getAnnotation(Inputs.class);
        final Output outputAnnotation = methodElement.getAnnotation(Output.class);
        final InvocationMode invocationMode =
                (invocationAnnotation != null) ? getInvocationMode(methodElement,
                                                                   invocationAnnotation) : null;
        InputMode inputMode = null;
        OutputMode outputMode = null;

        final List<? extends VariableElement> parameters = methodElement.getParameters();

        for (final VariableElement parameter : parameters) {

            final Input annotation = parameter.getAnnotation(Input.class);

            if (annotation == null) {

                continue;
            }

            inputMode = getInputMode(methodElement, annotation, parameter, parameters.size());
        }

        String method;

        if (inputsAnnotation != null) {

            if (outputAnnotation != null) {

                getOutputMode(methodElement, targetMethod);
            }

            if (!methodElement.getParameters().isEmpty()) {

                throw new IllegalArgumentException(
                        "methods annotated with " + Inputs.class.getSimpleName()
                                + " must have no input parameters: " + methodElement);
            }

            final TypeMirror returnType = methodElement.getReturnType();
            final TypeMirror returnErasure = typeUtils.erasure(returnType);

            if (!typeUtils.isAssignable(invocationChannelType, returnErasure)
                    && !typeUtils.isAssignable(routineType, returnErasure)) {

                throw new IllegalArgumentException(
                        "the proxy method has incompatible return type: " + methodElement);
            }

            inputMode = InputMode.CHANNEL;
            outputMode = OutputMode.CHANNEL;

            if (typeUtils.isAssignable(invocationChannelType, returnErasure)) {

                method = getMethodInputsChannelTemplate(methodElement, count);

            } else {

                method = getMethodInputsRoutineTemplate(methodElement, count);
            }

        } else if (outputAnnotation != null) {

            outputMode = getOutputMode(methodElement, targetMethod);
            final TypeMirror returnType = methodElement.getReturnType();
            final TypeMirror returnTypeErasure = typeUtils.erasure(returnType);

            if (returnType.getKind() == TypeKind.ARRAY) {

                targetReturnType = ((ArrayType) returnType).getComponentType();
                method = ((inputMode == InputMode.ELEMENT) && !typeUtils.isAssignable(
                        methodElement.getParameters().get(0).asType(), outputChannelType))
                        ? getMethodElementArrayTemplate(methodElement, count)
                        : getMethodArrayTemplate(methodElement, count);

            } else if (typeUtils.isAssignable(listType, returnTypeErasure)) {

                final List<? extends TypeMirror> typeArguments =
                        ((DeclaredType) returnType).getTypeArguments();

                if (typeArguments.isEmpty()) {

                    targetReturnType = objectType;

                } else {

                    targetReturnType = typeArguments.get(0);
                }

                method = ((inputMode == InputMode.ELEMENT) && !typeUtils.isAssignable(
                        methodElement.getParameters().get(0).asType(), outputChannelType))
                        ? getMethodElementListTemplate(methodElement, count)
                        : getMethodListTemplate(methodElement, count);

            } else if (typeUtils.isAssignable(outputChannelType, returnTypeErasure)) {

                final List<? extends TypeMirror> typeArguments =
                        ((DeclaredType) returnType).getTypeArguments();

                if (typeArguments.isEmpty()) {

                    targetReturnType = objectType;

                } else {

                    targetReturnType = typeArguments.get(0);
                }

                method = ((inputMode == InputMode.ELEMENT) && !typeUtils.isAssignable(
                        methodElement.getParameters().get(0).asType(), outputChannelType))
                        ? getMethodElementAsyncTemplate(methodElement, count)
                        : getMethodAsyncTemplate(methodElement, count);

            } else {

                throw new IllegalArgumentException("[" + methodElement + "] invalid return type");
            }

        } else if (isVoid) {

            method = ((inputMode == InputMode.ELEMENT) && !typeUtils.isAssignable(
                    methodElement.getParameters().get(0).asType(), outputChannelType))
                    ? getMethodElementVoidTemplate(methodElement, count)
                    : getMethodVoidTemplate(methodElement, count);

        } else {

            targetReturnType = methodElement.getReturnType();
            method = ((inputMode == InputMode.ELEMENT) && !typeUtils.isAssignable(
                    methodElement.getParameters().get(0).asType(), outputChannelType))
                    ? getMethodElementResultTemplate(methodElement, count)
                    : getMethodResultTemplate(methodElement, count);
        }

        if ((invocationMode == InvocationMode.PARALLEL) && (targetMethod.getParameters().size()
                > 1)) {

            throw new IllegalArgumentException(
                    "methods annotated with invocation mode " + InvocationMode.PARALLEL
                            + " must have no input parameters: " + methodElement);
        }

        final String resultClassName = getBoxedType(targetReturnType).toString();
        String methodHeader;
        methodHeader = getMethodHeaderTemplate(methodElement, count).replace("${resultClassName}",
                                                                             resultClassName);
        methodHeader = methodHeader.replace("${classFullName}", targetElement.asType().toString());
        methodHeader = methodHeader.replace("${methodCount}", Integer.toString(count));
        methodHeader = methodHeader.replace("${genericTypes}", buildGenericTypes(element));
        methodHeader = methodHeader.replace("${routineBuilderOptions}",
                                            buildRoutineOptions(methodElement));
        final SharedFields sharedFieldsAnnotation = methodElement.getAnnotation(SharedFields.class);

        if (sharedFieldsAnnotation != null) {

            final String[] names = sharedFieldsAnnotation.value();
            final StringBuilder builder = new StringBuilder("Arrays.asList(");
            final int length = names.length;

            for (int i = 0; i < length; ++i) {

                if (i != 0) {

                    builder.append(", ");
                }

                builder.append("\"").append(names[i]).append("\"");
            }

            builder.append(")");
            methodHeader = methodHeader.replace("${sharedFields}", builder.toString());

        } else {

            methodHeader = methodHeader.replace("${sharedFields}",
                                                "proxyConfiguration.getSharedFieldsOr(null)");
        }

        writer.append(methodHeader);
        method = method.replace("${resultClassName}", resultClassName);
        method = method.replace("${resultRawClass}", targetReturnType.toString());
        method = method.replace("${resultRawSizedArray}", buildSizedArray(targetReturnType));
        method = method.replace("${resultType}", methodElement.getReturnType().toString());
        method = method.replace("${methodCount}", Integer.toString(count));
        method = method.replace("${methodName}", methodElement.getSimpleName());
        method = method.replace("${params}", buildParams(methodElement));
        method = method.replace("${paramVars}", buildParamVars(methodElement));
        method = method.replace("${inputOptions}", buildInputOptions(methodElement, inputMode));
        method = method.replace("${inputParams}", buildInputParams(methodElement));
        method = method.replace("${invokeMethod}",
                                (invocationMode == InvocationMode.SYNC) ? "syncInvoke"
                                        : (invocationMode == InvocationMode.PARALLEL)
                                                ? "parallelInvoke" : "asyncInvoke");
        writer.append(method);
        String methodInvocationHeader;
        methodInvocationHeader = getMethodInvocationHeaderTemplate(methodElement, count);
        methodInvocationHeader = methodInvocationHeader.replace("${classFullName}",
                                                                targetElement.asType().toString());
        methodInvocationHeader =
                methodInvocationHeader.replace("${resultClassName}", resultClassName);
        methodInvocationHeader =
                methodInvocationHeader.replace("${methodCount}", Integer.toString(count));
        methodInvocationHeader =
                methodInvocationHeader.replace("${genericTypes}", buildGenericTypes(element));

        if (sharedFieldsAnnotation != null) {

            final String[] names = sharedFieldsAnnotation.value();
            final StringBuilder builder = new StringBuilder("Arrays.asList(");
            final int length = names.length;

            for (int i = 0; i < length; ++i) {

                if (i != 0) {

                    builder.append(", ");
                }

                builder.append("\"").append(names[i]).append("\"");
            }

            builder.append(")");
            methodInvocationHeader =
                    methodInvocationHeader.replace("${sharedFields}", builder.toString());

        } else {

            methodInvocationHeader = methodInvocationHeader.replace("${sharedFields}",
                                                                    "proxyConfiguration"
                                                                            + ".getSharedFieldsOr"
                                                                            + "(null)");
        }

        final boolean isStatic = targetMethod.getModifiers().contains(Modifier.STATIC);
        methodInvocationHeader = methodInvocationHeader.replace("${mutexTarget}", (isStatic)
                ? "target.getTargetClass()" : "target.getTarget()");
        writer.append(methodInvocationHeader);
        String methodInvocation;

        if ((inputMode == InputMode.COLLECTION) && (
                targetMethod.getParameters().get(0).asType().getKind() == TypeKind.ARRAY)) {

            final ArrayType arrayType = (ArrayType) targetMethod.getParameters().get(0).asType();
            methodInvocation = (isVoid) ? getMethodArrayInvocationVoidTemplate(methodElement, count)
                    : (outputMode == OutputMode.ELEMENT)
                            ? getMethodArrayInvocationCollectionTemplate(methodElement, count)
                            : getMethodArrayInvocationTemplate(methodElement, count);
            methodInvocation = methodInvocation.replace("${componentType}",
                                                        arrayType.getComponentType().toString());
            methodInvocation = methodInvocation.replace("${boxedType}", getBoxedType(
                    arrayType.getComponentType()).toString());

        } else {

            methodInvocation = (isVoid) ? getMethodInvocationVoidTemplate(methodElement, count)
                    : (outputMode == OutputMode.ELEMENT) ? getMethodInvocationCollectionTemplate(
                            methodElement, count)
                            : getMethodInvocationTemplate(methodElement, count);
        }

        methodInvocation =
                methodInvocation.replace("${classFullName}", targetElement.asType().toString());
        methodInvocation = methodInvocation.replace("${resultClassName}", resultClassName);
        methodInvocation = methodInvocation.replace("${methodCount}", Integer.toString(count));
        methodInvocation = methodInvocation.replace("${genericTypes}", buildGenericTypes(element));
        methodInvocation = methodInvocation.replace("${invocationTarget}",
                                                    (isStatic) ? typeUtils.erasure(
                                                            targetElement.asType()).toString()
                                                            : "((" + targetElement.asType()
                                                                                  .toString()
                                                                    + ") mTarget.getTarget())");
        methodInvocation =
                methodInvocation.replace("${targetMethodName}", targetMethod.getSimpleName());

        if (inputMode == InputMode.COLLECTION) {

            methodInvocation = methodInvocation.replace("${maxParamSize}",
                                                        Integer.toString(Integer.MAX_VALUE));
            methodInvocation = methodInvocation.replace("${paramValues}",
                                                        buildCollectionParamValues(targetMethod));

        } else {

            methodInvocation = methodInvocation.replace("${maxParamSize}", Integer.toString(
                    targetMethod.getParameters().size()));
            methodInvocation =
                    methodInvocation.replace("${paramValues}", buildParamValues(targetMethod));
        }

        writer.append(methodInvocation);
        String methodInvocationFooter;
        methodInvocationFooter = getMethodInvocationFooterTemplate(methodElement, count);
        methodInvocationFooter =
                methodInvocationFooter.replace("${resultClassName}", resultClassName);
        methodInvocationFooter =
                methodInvocationFooter.replace("${targetMethodName}", targetMethod.getSimpleName());
        methodInvocationFooter = methodInvocationFooter.replace("${targetMethodParamTypes}",
                                                                buildTargetParamTypes(
                                                                        targetMethod));
        writer.append(methodInvocationFooter);
    }
}
