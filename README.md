# ArkOfCode
Elasticsearch based code snippet storage application.

Developed for Masters Graduate Work at NUST MISiS

## System purpose 
Store, search, and provide data (code snippets). Like Pastebin, but with much wider search capabilities, as well as capable of working on servers running Russian ELBRUS processors

## Structure of system
The application consists of five microservices:
 - core - main service for working withsnippet . Their saving in the Elastic repository, the formation of search queries, etc.
 - user - service for storing and managing data about users and providing an interface for registration and authorization (JWT, Spring security)
 - eureka - a small microservice for registering and balancing REST requests between microservices.
 - gateway - a microservice that is an entry point for external requests and encapsulates the logic of internal communication between microservices.
 - backup - a service for automated data backup from elastic. Not really needed, I just wanted to work with something similar.

