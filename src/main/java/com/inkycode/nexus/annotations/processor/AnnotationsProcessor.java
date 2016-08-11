package com.inkycode.nexus.annotations.processor;

import static javax.lang.model.SourceVersion.RELEASE_8;

import java.util.Collections;

import javax.annotation.processing.SupportedSourceVersion;

import com.google.auto.common.BasicAnnotationProcessor;
import com.inkycode.nexus.annotations.processor.tasks.ServiceTask;

/**
 *
 * Executed at compile time and performs the service task.
 *
 */
@SupportedSourceVersion(RELEASE_8)
public class AnnotationsProcessor extends BasicAnnotationProcessor {

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        return Collections.singleton(new ServiceTask(this.processingEnv));
    }

}
