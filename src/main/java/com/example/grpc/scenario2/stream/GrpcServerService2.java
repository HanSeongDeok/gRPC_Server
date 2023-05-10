package com.example.grpc.scenario2.stream;

import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.chb.examples.lib.FitInjectionInfo;
import org.chb.examples.lib.HelloReply;
import org.chb.examples.lib.HelloRequest;
import org.chb.examples.lib.SimpleGrpc;

import java.io.IOException;
import java.util.*;

public class GrpcServerService2 {
    private int port;
    private Server server;

    public GrpcServerService2(int port) {
        this.port = port;
    }
    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new MyGrpcService())
                .build()
                .start();
        System.out.println("Server started, listening on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            stop();
        }));
    }
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private static class MyGrpcService extends SimpleGrpc.SimpleImplBase {
        @Override
        public StreamObserver<HelloRequest> streamFromClientToServer(StreamObserver<HelloReply> responseObserver) {
            return new StreamObserver<HelloRequest>(){
                @Override
                public void onNext(HelloRequest request) {
                    Map<String, FitInjectionInfo> map = new HashMap();
                    FitInjectionInfo injectInfo = FitInjectionInfo.newBuilder()
                            .setAddress("임의의 메모리 주소")
                            .setValue(3.25)
                            .build();
                    map.put("TC-001", injectInfo);

                    request.getTcInfoList().stream().forEach(v -> {
                        if (!map.containsKey(v.getTcName())) throw new IllegalStateException("해당 TC 미존재");
                        System.out.println(v.getTcName() + " Load Fault Injection Test Case Info (address, var)");
                        System.out.println("address : " + map.get(v.getTcName()).getAddress() + ", var : " + map.get(v.getTcName()).getValue());
                    });

                    HelloReply.Builder replyBuilder = HelloReply.newBuilder();
                    map.values().stream().forEach(v->replyBuilder.addInjectionInfo(v));
                    HelloReply reply = replyBuilder.build();
                    responseObserver.onNext(reply);
                }
                @Override
                public void onError(Throwable t) {

                }
                @Override
                public void onCompleted() {

                }
            };
        }
    }
}
