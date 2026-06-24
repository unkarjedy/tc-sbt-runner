addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.13")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.4")

resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Typesafe repository mwn" at "http://repo.typesafe.com/typesafe/maven-releases/"

resolvers += Resolver.mavenLocal

// TODO: replace this temporary Maven-local sbt-tc-logger PR 27 version with the published release version.
addSbtPlugin("org.jetbrains.teamcity.plugins.sbt" % "sbt-teamcity-logger" % "329645ca")
