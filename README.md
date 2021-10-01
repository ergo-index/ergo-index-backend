# Overview
This project provides the backend functionality for [https://ergo-index.fund](https://ergo-index.fund). It uses the Typelevel ecosystem ([http4s](https://http4s.org/), [cats](https://typelevel.org/cats/) / [cats-effect 3](https://typelevel.org/cats-effect/), [fs2](https://fs2.io/), [circe](https://circe.github.io/circe/)) and tries to use the latest functional programming approaches. Please feel free to open an issue if you notice anything that can be improved!

# Running the Application
See ADMINISTRATION.md for setup instructions. After one-time setup it will be as easy as `docker-compose up`, but there are important Firebase and security instructions you should follow first.

# Architecture
This is a [multi-project SBT build](https://www.scala-sbt.org/1.x/docs/Multi-Project.html), where each project is prefixed with a number that represents its layer. The higher layers depend on lower layers.

## Layer 1 (Ports)
Layer 1 projects contain interfaces ("ports") for Entities, EntityGateways (AKA repositories), and Controllers. They also contain a Boundary that is implemented purely in terms of the interfaces. Some of the Boundary and Controller functionality is redundant (e.g., a Boundary function just forwards the arguments to a Controller function with the same name). This is intentional to maintain optimal separation (see Entity-Control-Boundary section below).

## Layer 2 (Persistence, Delivery, and Adapters)
Layer 2 projects involve persisting data (i.e., implementing the Layer 1 EntityGateways), delivering/presenting the application, and providing implementations (AKA adapters) for the Layer 1 interfaces (AKA ports -- see ports and adapters architecture below). The only delivery that is currently supported is HTTP using http4s, so this delivery project maps endpoint routes to functions using Layer 1 Boundaries.

## Layer 3 (Main)
Layer 3 essentially contains assortments of dependencies that run the main program. It's easy to add other projects at this level (e.g., swap out auth_redis for auth_inmemory), but we only have use for one program so we only support that one here. The Layer 3 project that we support uses http4s for the delivery module (project called `02-delivery-http4s`), flat file storage using the BouncyCastle library for the keypair module (project called `02-persistence-keypair-bouncycastle_file`), the Ed25519 signature scheme for the JWT module (project called `02-adapter-jwt-ed25519`), and a Redis database for the auth module (project called `02-persistence-auth-redis`).

## Patterns
### Entity-Control-Boundary
We use a variation of the Entity-Control-Boundary (ECB) pattern, which is itself a variation of the MVC (Model-View-Controller) pattern. We have 4 layers of abstraction within the components:
1. Entity (AKA "model" in MVC): a model of some entity
2. EntityGateway (AKA "repository"): typically provides CRUD operations for storing, retrieving, and possibly transforming an Entity
3. Controller: provides business logic, usually using an EntityGateway and sometimes one or more Boundaries from other components
4. Boundary: provides a layer of abstraction over a Controller, and sometimes adds additional functionality using only the Controller

These layers interact with each other according to the following rules:
* Each Boundary is implemented purely in terms of a single Controller.
* Each Controller only interacts with EntityGateways and Boundaries
* EntityGateways only interact with Entities

### Hexagonal (Ports and Adapters) and Service-Oriented Architecture
We borrow from these architectures, but we don't adhere strictly to either of them. For example, we group our components into 3 layers of abstraction -- which goes against hexagonal architecture practices -- because it's easier to navigate that kind of file structure than an onion or a hexagon structure. However, we adopt the ports and adapters terminology from hexagonal architecture, and we adhere to the guiding principle that both architectures promote: maintaining separation of concerns and loosely coupled components. In this project, Layer 3 programs should be able to choose any combination of Layer 2's "adapters" (implementations) to plug in to Layer 1's "ports" (interfaces).

## Diagrams
There are diagrams available for each outer-most functionality of the program. In other words, each entry point through which an "Actor" (in the Entity-Control-Boundary model) would interact with the program is documented below.

### Initialize Program
TODO
### Log In
TODO
### Sign Up
TODO

# Design Patterns and Miscellaneous Implementation Decisions

## Bifunctor IO with Throwable
We heavily use the `EitherT` monad transformer to capture error types, which has a slightly negative effect on performance but is good enough in for our purposes. The `E` in `EitherT[IO, E, A]` handles recoverable errors, whereas the `IO` has a `Throwable` baked in to handle non-recoverable errors.

## No Tagless Final
We prefer a more concrete application that is bound to cats-effect IO over the noise that Tagless Final would create. This means that it would take more effort to switch our effect monad, but that's fine for this application. We may possibly switch to ZIO at some point as the library improves and provides more built-in features like logging and metrics.

# Style Guide
* We use scalafmt to maintain a consitent style. This should be applied automatically in most IDEs using the provided `.scalafmt.conf` file.
* We prefer documenting a trait with special notes about different functions instead of polluting each function with ScalaDocs. If the functions are written well, then you can usually infer their meaning from their types and names.
