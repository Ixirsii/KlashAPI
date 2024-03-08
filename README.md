![KlashAPI logo](/src/main/resources/KlashAPI.png)

[![GitHub release](https://img.shields.io/github/release/Ixirsii/KlashAPI.svg?style=flat-square)](https://github.com/Ixirsii/KlashAPI/releases/latest)
[![Codecov](https://img.shields.io/codecov/c/github/Ixirsii/KlashAPI?logo=codecov&style=flat-square)](https://codecov.io/gh/Ixirsii/KlashAPI)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/Ixirsii/KlashAPI/codecov.yml?branch=main&logo=github&style=flat-square)](https://github.com/Ixirsii/KlashAPI/actions?workflow=codecov)

# Buzzword Soup!

KlashAPI is a reactive, functional API wrapper for Clash of Clans written in
Kotlin. It uses [Project Reactor](https://projectreactor.io/) and
[Arrow](https://arrow-kt.io/) to provide a functional, reactive API.

[Project Reactor](https://projectreactor.io/) is used to provide a reactive,
asynchronous API.

[Arrow](https://arrow-kt.io/) is used primarily for error handling. Instead of
throwing exceptions like in Java, we return and `Either` type, which contains
either an error or the result of the request. This allows you to handle errors
without the overhead cost of unwrapping the stack when an exception is thrown,
and prevents the need for try-catch blocks at the cost of mapping the `Either`.

# Quick start

## Installation

**TODO:** Upload build artifacts to Maven Central.

### Gradle

```kotlin
dependencies {
    implementation("tech.ixirsii:klash-api:0.0.1")
}
```

## Usage

### Authenticating

There are two ways to authenticate with KlashAPI: using a token or by providing
developer portal login credentials (which will generate tokens for you).

#### Using a token (recommended)

1. Go to the [developer portal](https://developer.clashofclans.com) and create
   an account.
2. Generate a new token.
3. Pass the token to the `ClashAPI` constructor.

```kotlin
val token: String = System.getenv()["CLASH_API_TOKEN"] ?: error("No token found")
val clashAPI = ClashAPI(token)
```

#### Using developer portal credentials

This method generates API tokens for you, which is useful if you don't know
the IP address of the machine you're running on (such as running GitHub
Actions). Note however that this constructor does not return a `ClashAPI`,
it returns an `Either<ClashTokenError, ClashAPI>`. This is because the
request to get or create a token may fail, preventing the API from
authenticating.

1. Go to the [developer portal](https://developer.clashofclans.com) and create
   an account.
2. Pass your credentials to the `ClashAPI` "constructor".
3. Unwrap the `Either` and handle the error if it's a `Left`.

```kotlin
val email: String = System.getenv()["CLASH_API_EMAIL"] ?: error("No email found")
val password: String = System.getenv()["CLASH_API_PASSWORD"] ?: error("No password found")
val clashAPI = ClashAPI(email, password).onRight {
   // Run program
}.onLeft {
   // Handle error
}
```

### Making requests

Most of the APIs take a tag parameter. This is the player, clan, or clan war
tag. In game these are prefixed with a '#' but you should omit this when
passing the tag to the API.

#### Synchronous

`KlashAPI` uses [Project Reactor](https://projectreactor.io/) to provide an
asynchronous API. However, if you want to make synchronous requests, you can
simply call the `block()` method on the `Mono` returned by the request.

```kotlin
val either: Either<ClashAPIError, Clan> = clashAPI.getClan(clanTag).block()!!

either.onRight { clan: Clan ->
    val isFamilyFriendly: Boolean = clan.isFamilyFriendly
    val clanCapitalPoints: Int = clan.clanCapitalPoints
}.onLeft { error: ClashAPIError ->
    println(error)
}
```

#### Asynchronous

```kotlin
underTest.clan(clanTag).subscribe { either ->
    either.onRight { clan: Clan ->
        val isFamilyFriendly: Boolean = clan.isFamilyFriendly
        val clanCapitalPoints: Int = clan.clanCapitalPoints
    }.onLeft { error: ClashAPIError ->
        println(error)
    }
}
```

# Contributing

## Running tests

If you know the IP address of the machine you're running on, set the `API_KEY`
environment variable to your token. Otherwise, set the `API_EMAIL` and
`API_PASSWORD` environment variables to your developer portal credentials.

## Configuring GitHub Actions

### Pre-requisite

To add an environment variable in GitHub, go to the settings for your fork,\
select "Secrets and variables" under the Security section, then add a new
repository secret.

### Set environment variables

To run the codecov action, you need to get a token from
[Codecov](https://codecov.io/), then set the `CODECOV_TOKEN` environment
variable. You'll also need to set 3 more environment variables:`API_EMAIL`,
`API_PASSWORD` and `API_TOKEN`.

#### CODECOV_TOKEN

This one is pretty self-explanatory, it's the token you got from
[Codecov](https://codecov.io/) earlier.

#### API_EMAIL

This is the email address you used to sign up for the Clash of Clans developer
portal.

#### API_PASSWORD

This is the password you used to sign up for the Clash of Clans developer

#### API_TOKEN

This is a player token, generated in game. To get one, open Clash of Clans,
go to settings, tap "More Settings", scroll to the bottom, and tap on "Show"
under API Token.
