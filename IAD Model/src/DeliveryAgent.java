import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeliveryAgent extends Agent {
    int x;
    int y;
    private List<AID> visitedPoints = new ArrayList<>();
    private Set<AID> allPoints = new HashSet<>();
    AID nearestPoint;
    protected void setup() {
        // Initialisation de l'agent DeliveryAgent
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            x = Integer.parseInt(args[0].toString());
            y = Integer.parseInt(args[1].toString());
        } else {
            System.err.println("VehicleAgent requires x and y coordinates as arguments.");
            doDelete();
        }
        System.out.println(getLocalName() + ": Initial position: x: " + x + ", y: " + y);
        // Route planning behavior
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        addBehaviour(new VisitAllPointsBehaviour());
    }

    private class VisitAllPointsBehaviour extends CyclicBehaviour {
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Get all Point agents
                    AID[] pointAgents = searchAgents("Point*");
                    for (AID pointAgent : pointAgents) {
                        allPoints.add(pointAgent);
                    }
                    step++;
                    break;
                case 1:
                    // Find the nearest unvisited point
                    AID nearestPoint = findNearestUnvisitedPoint();
                    if (nearestPoint != null) {
                        visitedPoints.add(nearestPoint);
                        System.out.println(getLocalName() + ": I will deliver now: " + nearestPoint.getLocalName());

                        // Log the names of visited points
                        List<String> visitedPointNames = new ArrayList<>();
                        for (AID visitedPoint : visitedPoints) {
                            visitedPointNames.add(visitedPoint.getLocalName());
                        }
                        //System.out.println(getLocalName() + ": Visited points so far: " + visitedPointNames.toString());
                        informCoordinator(visitedPointNames);
                        // Propose to visit the point
                        ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
                        propose.addReceiver(nearestPoint);

                        String proposeContent = "Propose:" + calculateDistance(nearestPoint) + ":" + visitedPoints.toString();

                        propose.setContent(proposeContent);
                        send(propose);

                        // Wait for the response from the chosen point
                        MessageTemplate proposeTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        MessageTemplate refuseTemplate = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
                        ACLMessage response = receive(MessageTemplate.or(proposeTemplate, refuseTemplate));

                        if (response != null && response.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                            System.out.println(getLocalName() + ": Proposal accepted by " + response.getSender().getLocalName());
                            indicatePath(nearestPoint);

                            // Update the position after visiting the point
                            updatePosition(nearestPoint);
                        } else {
                            //System.out.println(getLocalName() + ": Proposal rejected or no response received.");
                        }
                    } else {
                        // No unvisited points left, inform all Point agents
                        informAllPoints();
                        step++;
                    }
                    break;
            }
        }
        private void indicatePath(AID nearestPoint) {
            String[] coordinates = getPointCoordinates(nearestPoint);

            if (coordinates.length == 2) {
                int destinationX = Integer.parseInt(coordinates[0]);
                int destinationY = Integer.parseInt(coordinates[1]);

                System.out.println(getLocalName() + ": Indicating how much time to arrive to " + nearestPoint.getLocalName());

                // Coordinates of the vehicle
                int currentX = x;
                int currentY = y;

                // Move towards the destination
                while (currentX != destinationX || currentY != destinationY) {
                    // Update the vehicle's coordinates at each step
                    if (currentX < destinationX) {
                        currentX++;
                    } else if (currentX > destinationX) {
                        currentX--;
                    }

                    if (currentY < destinationY) {
                        currentY++;
                    } else if (currentY > destinationY) {
                        currentY--;
                    }

                    // Print the current position
                    System.out.println(getLocalName() + ": I will arrive to "+ nearestPoint.getLocalName() +" in "+ currentX + " minutes");
                }

                // Arrived at the destination
                System.out.println(getLocalName() + ": Arrived at " + nearestPoint.getLocalName());

                // Update the vehicle's final coordinates
                x = currentX;
                y = currentY;
            } else {
                System.out.println(getLocalName() + ": Invalid coordinates format received from " + nearestPoint.getLocalName());
            }
        }

        private AID findNearestUnvisitedPoint() {
            AID nearestPoint = null;
            double minDistance = Double.MAX_VALUE;

            for (AID point : allPoints) {
                double distance = calculateDistance(point);
                if (!visitedPoints.contains(point) && distance < minDistance) {
                    nearestPoint = point;
                    minDistance = distance;
                }
            }

            return nearestPoint;
        }

        private double calculateDistance(AID point) {
            // Get the coordinates of the Point agent
            String[] coordinates = getPointCoordinates(point);
            int pointX = Integer.parseInt(coordinates[0]);
            int pointY = Integer.parseInt(coordinates[1]);

            // Calculate the distance
            return Math.sqrt(Math.pow(pointX - x, 2) + Math.pow(pointY - y, 2));
        }

        private String[] getPointCoordinates(AID point) {
            // Send a request to the Point agent to get its coordinates
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setContent("Position?");
            request.addReceiver(point);
            send(request);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Wait for the response from the Point agent
            ACLMessage response = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

            if (response != null) {
                return response.getContent().split(",");
            }

            return new String[]{"0", "0"}; // Default coordinates if no response
        }

        private void informAllPoints() {
            // Send an INFORM message to all Point agents to inform them that all points have been visited
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.setContent("AllPointsVisited");
            AID[] allPointAgents = searchAgents("Point*");

            if (allPointAgents != null) {
                for (AID pointAgent : allPointAgents) {
                    inform.addReceiver(pointAgent);
                }
                send(inform);
                ACLMessage inform1 = new ACLMessage(ACLMessage.INFORM);
                inform1.setContent("finish");
                AID coordinatorAgent = getCoordinatorAgent();
                if (coordinatorAgent != null) {
                    inform1.addReceiver(coordinatorAgent);
                    send(inform1);
                }
                System.out.println(getLocalName() + ": Coordinator! All points have been visited.");
            }
        }
        private void updatePosition(AID point) {
            // Update the position of the vehicle agent after visiting the point
            String[] coordinates = getPointCoordinates(point);
            x = Integer.parseInt(coordinates[0]);
            y = Integer.parseInt(coordinates[1]);
        }
    }
    private AID getCoordinatorAgent() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Coordinator");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                return result[0].getName();
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        return null;
    }
    private void informCoordinator(List<String> visitedPointNames) {
        // Send an INFORM message to the Coordinator agent
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.setContent("VisitedPoints:" + visitedPointNames.toString());
        AID coordinatorAgent = getCoordinatorAgent();
        if (coordinatorAgent != null) {
            inform.addReceiver(coordinatorAgent);
            send(inform);
        }
    }
    private AID[] searchAgents(String agentNamePattern) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentNamePattern);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            AID[] agents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                agents[i] = result[i].getName();
            }
            return agents;
        } catch (FIPAException fe) {
            fe.printStackTrace();
            return null;
        }
    }
}
