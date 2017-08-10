OPi+
=======

OPi+ is an open source prototype tool to conduct experiments with change-driven mutation testing. In this environment, we adapted current technologies to meet the requirements for continuous mutation testing by customizing the core of the tool or extending the features with a custom proxy. The way we integrate continuous mutation testing with the flow of modern software development mimics a related project [Operias](https://github.com/SERG-Delft/operias).

Currently, OPi+ supports only maven based Java projects hosted on Github.


Running OPi+
=======

The purpose of this study is to analyze whether mutation testing can improve the development process by integrating it in a CI environment. In order to conduct more evaluations start in the EvaluationRunner class. There all setup data must be inserted. It currently contains an example setup for [Apache Commons IO](https://github.com/apache/commons-io).

