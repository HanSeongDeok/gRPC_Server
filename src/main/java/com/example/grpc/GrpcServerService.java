package com.example.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.chb.examples.lib.HelloReply;
import org.chb.examples.lib.HelloRequest;
import org.chb.examples.lib.SimpleGrpc;


import java.util.Optional;

@GrpcService
public class GrpcServerService extends SimpleGrpc.SimpleImplBase {
    @Override
    // 클라이언트에서 HelloRequest 직렬화된 데이터를 받아와서(역질렬화) 로직을 처리하고
    // StreamObserver<HelloReply> 직렬화된 데이터로 반환
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        // Builder 를 통해 객체를 직렬화 함.
        // 클라이언트 에서 받아온 객체로 로직을 처리하고 Protocol Buffers 형식으로 직렬화
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello ==> " + request.getName())
                .build();
        // 클라이언트에게 호출
        responseObserver.onNext(reply);
        // 완료 신호 전송
        responseObserver.onCompleted();
    }

   /* @Override
    public void streamFromServerToClient(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello, " + request.getName()).build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Welcome to gRPC!").build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("How are you doing today?").build());
        responseObserver.onNext(HelloReply.newBuilder().setMessage("Have a nice day!").build());
        responseObserver.onCompleted();
    }*/
    @Override
    public StreamObserver<HelloRequest> streamFromClientToServer(StreamObserver<HelloReply> responseObserver) {
        return new StreamObserver<HelloRequest>() {
            @Override
            public void onNext(HelloRequest request) {
                // 클라이언트에서 요청한 데이터 처리 로직 구현
                if(!request.getObject().isEmpty()) {
                    Optional.ofNullable(request.getObject()).ifPresent(json -> {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            // JSON 파싱
                            JsonNode rootNode = objectMapper.readTree(json);
                            String name = rootNode.get("name").asText();
                            responseObserver.onNext(HelloReply.newBuilder().setMessage("Fault Injection Name, " + name).build());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello, " + request.getName()).build());
                }
            }
            @Override
            public void onError(Throwable throwable) {
                // 클라이언트에서 오류 발생 시 처리 로직 구현
            }
            @Override
            public void onCompleted() {
                // 클라이언트에서 요청 종료 시 처리 로직 구현
                // 그리고 내부에서 또한 클라이언트와 통신을 종료하는 로직의 코드가 구현되어 있음
                System.out.println("gRPC 서버 단 종료!");
                responseObserver.onCompleted();
            }
        };
    }
}