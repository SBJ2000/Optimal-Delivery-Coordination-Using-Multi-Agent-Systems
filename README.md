# Optimal Delivery Coordination Using Multi-Agent Systems

![Project Logo](https://github.com/SBJ2000/Optimal-Delivery-Coordination-Using-Multi-Agent-Systems/blob/main/Images/Logo.jpg)

## Description and Objectives
The application simulates a multi-agent system where Client agents, Delivery agents, and Coordinator agents interact to deliver parcels to various clients in an optimal order and reduced time.

The main objectives of the application are:

* Distributed resolution of a search and optimization problem: Client agents cooperate with the Delivery agent to find the nearest point involving the Coordinator agent.
* Forms of interaction: communication, cooperation between agents in problem-solving, and action coordination. Agents communicate to exchange information and coordinate their actions.

## Agents Developed

### Client Agent
* Type: Cognitive agent
* Structure: Coordinates x and y
* Behavior: Respond to coordinate requests from other agents and participate in a proposal and acceptance process with a Delivery agent.
### Delivery Agent
* Type: Cognitive agent
* Structure: Coordinates x and y
* Behavior: Plan its route by choosing the nearest client among the Client agents and repeat the process after each delivery.
### Coordinator Agent
* Type: Cognitive agent
* Structure: Activated by message
* Behavior: Coordinate and supervise the Delivery agents by receiving updates on visited points and handling end-of-mission messages.

## Implementation
The project is divided into three classes written in Java using the JADE (Java Agent DEvelopment Framework) as follows:

1- ClientAgent: Handles the client agents' initialization, registration, and coordinate response behavior.

2- DeliveryAgent: Manages the delivery agents' initialization, route planning, and delivery actions.

3- CoordinatorAgent: Coordinates the actions of the delivery agents and handles updates on the visited points.

## Installation

1- Clone the repository:

    git clone https://github.com/username/multi-agent-delivery-system.git
    cd multi-agent-delivery-system

2- Install JADE:
* Download the JADE library from the official website.
* Add JADE to your project's classpath.

3-Compile the project:

    java -cp .:lib/jade.jar jade.Boot -gui

## Usage

1- Run the JADE platform:

    java -cp .:lib/jade.jar jade.Boot -gui

2- Start the agents:

    java -cp .:lib/jade.jar:. ClientAgent x y
    java -cp .:lib/jade.jar:. DeliveryAgent x y
    java -cp .:lib/jade.jar:. CoordinatorAgent

Replace x and y with the initial coordinates for the Client and Delivery agents.

## Example Execution

The project can be executed with specific parameters to simulate the interaction between agents. For example:

The Delivery agent moves towards the nearest client and updates its position after each delivery, coordinating with the Coordinator agent by exchanging messages indicating the remaining time to reach the next client.

![Project Logo](https://github.com/SBJ2000/Optimal-Delivery-Coordination-Using-Multi-Agent-Systems/blob/main/Images/ExampleOfUsage.png)


## Interaction Diagrams

Client, Delivery, and Coordinator agents interact via ACL messages. The messages exchanged include:

* "Position?" (Request and Reply): Sent by a Delivery agent to request the coordinates of a Client agent.
* "Propose": Sent by a Delivery agent to propose a destination point to a Client agent.
* "Accept_Proposal": Sent by a Client agent to accept the proposal from a Delivery agent.
* "Reject_Proposal": Sent by a Client agent to reject the proposal from a Delivery agent.
* "Inform": Sent by a Delivery agent to the Coordinator agent to inform about the selected destination point and the remaining time to reach it.

## Conclusion

This project presents a solution to a navigation problem in a dynamic environment using autonomous agents capable of communicating and negotiating routes. The solution is flexible and can be adapted to various types of environments and constraints, such as taxi drivers.