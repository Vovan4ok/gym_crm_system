# grpc-contract

The protobuf contract of the internal gRPC API between gym-service (client) and
workload-service (server), shared by both as a Maven dependency. The module
holds only `src/main/proto/workload.proto`; the Java classes are generated at
build time and never committed.

## The contract
```
service WorkloadService {
  rpc GetWorkload(GetWorkloadRequest) returns (GetWorkloadResponse);   // unary
}
```
`GetWorkloadResponse` mirrors workload-service's REST summary: trainer
username, first/last name, `active`, and `years → months → summary_duration`
(minutes). The month is the `CalendarMonth` enum: `MONTH_UNSPECIFIED = 0`
(proto3 enums must start at 0) and `MONTH_JANUARY = 1` … `MONTH_DECEMBER = 12`,
numbered like `java.time.Month#getValue()` so both sides convert by number.

- proto package `org.volodymyrzganiaiko.workload_service.v1`: the `v1` leaves
  room for a breaking `v2` that can be served side by side;
- `java_package` `org.volodymyrzganiaiko.workload_service.grpc`, with
  `java_multiple_files = true` (one Java file per message).

## Why a separate module
One `.proto`, one source of truth: client and server are always built from the
same contract, so they cannot drift apart. The module depends on no framework
(no Spring), only on the gRPC and protobuf runtime libraries.

## Code generation
`io.github.ascopes:protobuf-maven-plugin` runs `protoc` plus the
`protoc-gen-grpc-java` plugin in `generate-sources`, producing the message
classes (immutable, with builders) and `WorkloadServiceGrpc` (`ImplBase` for
the server, blocking/future/async stubs for clients). Maven downloads `protoc`
for the current OS; nothing is installed by hand.

**Versions, one place to change.** The parent pom imports the Spring gRPC BOM
(`spring-grpc.version`, 0.12.x: the last line for Spring Boot 3.5), which
manages the gRPC and protobuf *libraries*. A BOM cannot manage plugin
configuration, so the two *tool* versions are parent-pom properties that must
match that BOM:

| Property | Must equal the BOM's |
|---|---|
| `protoc.version` | `protobuf-java` |
| `grpc.version` | `grpc-*` |

Generated protobuf 4.x code checks the runtime at startup and fails if `protoc`
is newer than `protobuf-java`, so bump all three together.

`javax.annotation-api` is `provided`: the generated stubs carry
`@javax.annotation.Generated`, which has SOURCE retention, so it is needed only
to compile them and must not leak onto the services' runtime classpath.

## Evolving the contract safely
Only the **field number** travels on the wire, never the name:

- adding a field (with a new number) is safe: old readers skip it, new readers
  of old data see the default value;
- renaming a field is safe on the wire;
- **never** change a field's number or type, and never reuse a number: mark a
  removed field `reserved` (number and name) so it cannot come back by accident;
- proto3 has no `null`: an unset field reads as `0`, `""`, `false` or an empty
  list. "Not found" is a gRPC status (`NOT_FOUND`), not an empty message;
- adding an enum value is safe for old clients only if they handle unknown
  values: gym-service reads the raw number (`getMonthValue()`) and skips months
  it does not know instead of failing.
