package com.example.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.chb.examples.lib.HelloReply;
import org.chb.examples.lib.HelloRequest;
import org.chb.examples.lib.SimpleGrpc;

@GrpcService
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void streamFromServerToClient(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello, " + request.getName()).build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Welcome to gRPC!").build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("How are you doing today?").build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Have a nice day!").build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<HelloRequest> streamFromClientToServer(StreamObserver<HelloReply> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest request) {
                // 클라이언트에서 요청한 데이터 처리 로직 구현
                responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello, " + request.getName()).build());
            }
            @Override
            public void onError(Throwable throwable) {
                // 클라이언트에서 오류 발생 시 처리 로직 구현
            }
            @Override
            public void onCompleted() {
                // 클라이언트에서 요청 종료 시 처리 로직 구현
                responseObserver.onCompleted();
            }
        };
    }
}
