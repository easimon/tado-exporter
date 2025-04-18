# Changelog

## [6.4.0](https://github.com/easimon/tado-exporter/compare/6.3.1...6.4.0) (2025-03-15)


### Features

* introduce a rate limit of one attempt per minute on authentication ([c530643](https://github.com/easimon/tado-exporter/commit/c530643fbe30de5f9cf658f5eb2a8d14bab1e535))

## [6.3.1](https://github.com/easimon/tado-exporter/compare/6.3.0...6.3.1) (2025-01-12)


### Bug Fixes

* add explicit JsonCreator to Zones class, required by current Jackson ([58f9a6f](https://github.com/easimon/tado-exporter/commit/58f9a6fa2e16c4187238f9a2c1a31eb5f05233ad))

## [6.3.0](https://github.com/easimon/tado-exporter/compare/6.2.5...6.3.0) (2024-06-24)


### Features

* upgrade to spring boot 3.3, rework value-filtering registry ([1c602b3](https://github.com/easimon/tado-exporter/commit/1c602b3d07c3feee765981c0944219ccfdfeaf5e))

## [6.2.5](https://github.com/easimon/tado-exporter/compare/v6.2.4...6.2.5) (2024-02-04)


### Bug Fixes

* fuck you, release-please-action! ([1087107](https://github.com/easimon/tado-exporter/commit/1087107b691a7dea4d7ddb9a813b4a5fd694d0a6))
* trigger release build, updating dependencies ([1276cc7](https://github.com/easimon/tado-exporter/commit/1276cc7d102ce28b30bd132620652f77867b6ad1))
* update to release-please-action 4 ([fb1d719](https://github.com/easimon/tado-exporter/commit/fb1d719fa8a1dc4d788e4e93c97b31354b4b9db1))

## [6.2.4](https://github.com/easimon/tado-exporter/compare/v6.2.3...v6.2.4) (2024-02-04)


### Bug Fixes

* update to release-please-action 4 ([fb1d719](https://github.com/easimon/tado-exporter/commit/fb1d719fa8a1dc4d788e4e93c97b31354b4b9db1))

## [6.2.3](https://github.com/easimon/tado-exporter/compare/6.2.2...v6.2.3) (2024-02-04)


### Bug Fixes

* trigger release build, updating dependencies ([1276cc7](https://github.com/easimon/tado-exporter/commit/1276cc7d102ce28b30bd132620652f77867b6ad1))

## [6.2.2](https://github.com/easimon/tado-exporter/compare/6.2.1...6.2.2) (2023-11-06)


### Bug Fixes

* trigger release for dependency updates ([91de83d](https://github.com/easimon/tado-exporter/commit/91de83dd66c53f1961fd49f0423df9ecfd94671e))

## [6.2.1](https://github.com/easimon/tado-exporter/compare/6.2.0...6.2.1) (2023-06-08)


### Bug Fixes

* trigger release for dependency updates ([358ecf0](https://github.com/easimon/tado-exporter/commit/358ecf02f43cd2ba1e7fde3bad62c2b44150bfd9))

## [6.2.0](https://github.com/easimon/tado-exporter/compare/6.1.2...6.2.0) (2023-04-13)


### Features

* suppress metrics for unreachable devices instead reporting NaN ([638e413](https://github.com/easimon/tado-exporter/commit/638e413a7f400c7963d2fb8efa5592730508caee))

## [6.1.2](https://github.com/easimon/tado-exporter/compare/6.1.1...6.1.2) (2023-03-28)


### Miscellaneous Chores

* release 6.1.2 ([8e45dd5](https://github.com/easimon/tado-exporter/commit/8e45dd5da9c6f0820d21bd44dc018ea40efa5ea3))

## [6.1.1](https://github.com/easimon/tado-exporter/compare/6.1.0...6.1.1) (2023-01-30)


### Bug Fixes

* stabilize zone model entries (e.g. on battery state changes) ([bd51bea](https://github.com/easimon/tado-exporter/commit/bd51bea5d5d45f62a186d1e21220354a0fd18a8e))

## [6.1.0](https://github.com/easimon/tado-exporter/compare/6.0.0...6.1.0) (2023-01-29)


### Features

* debug logging for meter update ([6238770](https://github.com/easimon/tado-exporter/commit/62387705ec066e7132967e721e2a150c90bb6b55))
* switch from tomcat to undertow ([c9e8e64](https://github.com/easimon/tado-exporter/commit/c9e8e644f1b56b63a6ac43da255e4916cfe768ac))

## [6.0.0](https://github.com/easimon/tado-exporter/compare/5.1.0...6.0.0) (2022-12-30)


### ⚠ BREAKING CHANGES

* upgrade to spring boot 3

### Features

* upgrade to spring boot 3 ([9612b52](https://github.com/easimon/tado-exporter/commit/9612b52d2186a3176681f4b9d2370f35003ae71b))

## [5.1.0](https://github.com/easimon/tado-exporter/compare/5.0.0...5.1.0) (2022-11-05)


### Features

* add boolean status for presence ([a77fc97](https://github.com/easimon/tado-exporter/commit/a77fc97e89d7ba92582805c00491b0509c9959ba))

## [5.0.0](https://github.com/easimon/tado-exporter/compare/4.0.0...5.0.0) (2022-09-22)


### ⚠ BREAKING CHANGES

* migrate to spring boot

### Features

* migrate to spring boot ([0bb681c](https://github.com/easimon/tado-exporter/commit/0bb681c5d5de4c688a5a1e32aa55339ad1448501))
* update mockk ([77da7a7](https://github.com/easimon/tado-exporter/commit/77da7a7f0cdf04d996b1e88a40c11f8f2378da90))


### Bug Fixes

* pom.xml to reduce vulnerabilities ([4541d65](https://github.com/easimon/tado-exporter/commit/4541d65c2689018d8ffc43516b627d1af442c0bf))
