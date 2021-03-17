package io.quarkiverse.rsocket;

import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import io.quarkiverse.rsocket.deployment.RSocketBuildItem;
import io.quarkiverse.rsocket.deployment.RoutedRsocketHandlerBuildItem;
import io.quarkiverse.rsocket.runtime.RSocketConfig;
import io.quarkiverse.rsocket.runtime.RSocketRecorder;
import io.quarkiverse.rsocket.runtime.RoutedRsocket;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.runtime.RuntimeValue;
import io.rsocket.frame.FrameType;

public class RsocketProcessor {

    public static final DotName RSOCKET = DotName.createSimple("io.rsocket.RSocket");
    public static final DotName FIRE_AND_FORGET_HANDLER = DotName
            .createSimple("io.quarkiverse.rsocket.runtime.FireAndForgetHandler");
    public static final DotName REQUEST_CHANNEL_HANDLER = DotName
            .createSimple("io.quarkiverse.rsocket.runtime.RequestChannelHandler");
    public static final DotName REQUEST_RESPONSE_HANDLER = DotName
            .createSimple("io.quarkiverse.rsocket.runtime.RequestResponseHandler");
    public static final DotName REQUEST_STREAM_HANDLER = DotName
            .createSimple("io.quarkiverse.rsocket.runtime.RequestStreamHandler");
    public static final DotName PATH = DotName.createSimple("javax.ws.rs.Path");
    private static final Logger LOGGER = Logger.getLogger(RsocketProcessor.class);
    private static final String FEATURE = "rsocket";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void configure(CombinedIndexBuildItem combinedIndexBuildItem,
            BuildProducer<RSocketBuildItem> rsockets,
            BuildProducer<RoutedRsocketHandlerBuildItem> handlers) {
        IndexView index = combinedIndexBuildItem.getIndex();
        for (ClassInfo classInfo : index.getAllKnownImplementors(RSOCKET)) {
            rsockets.produce(new RSocketBuildItem(classInfo.name().toString()));
            LOGGER.info("add rsocket " + classInfo.name().toString());
            //quick hack to only load first rsocket and skip route if rsocket
            return;
        }
        for (ClassInfo classInfo : index.getAllKnownImplementors(FIRE_AND_FORGET_HANDLER)) {
            AnnotationInstance pathAnnotationInstance = classInfo.classAnnotation(PATH);
            String path = pathAnnotationInstance.value().asString();
            handlers.produce(new RoutedRsocketHandlerBuildItem(path, classInfo.name().toString(), FrameType.REQUEST_FNF));
            LOGGER.info("add handler " + classInfo.name().toString());
        }
        for (ClassInfo classInfo : index.getAllKnownImplementors(REQUEST_CHANNEL_HANDLER)) {
            AnnotationInstance pathAnnotationInstance = classInfo.classAnnotation(PATH);
            String path = pathAnnotationInstance.value().asString();
            handlers.produce(new RoutedRsocketHandlerBuildItem(path, classInfo.name().toString(), FrameType.REQUEST_CHANNEL));
            LOGGER.info("add handler " + classInfo.name().toString());
        }
        for (ClassInfo classInfo : index.getAllKnownImplementors(REQUEST_RESPONSE_HANDLER)) {
            AnnotationInstance pathAnnotationInstance = classInfo.classAnnotation(PATH);
            String path = pathAnnotationInstance.value().asString();
            handlers.produce(new RoutedRsocketHandlerBuildItem(path, classInfo.name().toString(), FrameType.REQUEST_RESPONSE));
            LOGGER.info("add handler " + classInfo.name().toString());
        }
        for (ClassInfo classInfo : index.getAllKnownImplementors(REQUEST_STREAM_HANDLER)) {
            AnnotationInstance pathAnnotationInstance = classInfo.classAnnotation(PATH);
            String path = pathAnnotationInstance.value().asString();
            handlers.produce(new RoutedRsocketHandlerBuildItem(path, classInfo.name().toString(), FrameType.REQUEST_STREAM));
            LOGGER.info("add handler " + classInfo.name().toString());
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void startServer(RSocketRecorder recorder,
            RSocketConfig config,
            RSocketBuildItem rsockets,
            List<RoutedRsocketHandlerBuildItem> handlers) {
        LOGGER.info("start rsocket server ");
        if (rsockets != null) {
            recorder.initServer(config, rsockets.getRSocketClassName());
            LOGGER.info("rsocket server started on " + rsockets.getRSocketClassName());
        } else if (handlers.size() > 0) {
            RuntimeValue<RoutedRsocket.Builder> builder = recorder.buildRouter();
            for (RoutedRsocketHandlerBuildItem handler : handlers) {
                recorder.addHandler(builder, handler.getPath(), handler.getHandler(), handler.getFrameType());
            }
            recorder.initServer(config, builder);
        }
    }
}
