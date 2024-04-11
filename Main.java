// Project 2 - Genetic Algorithms by Trinity McCann
// This project demonstrates a low cost schedule generator using a genetic algorithm. 

import java.util.*;

// Creates the Activity class, which holds expected enrollment and preferred facilitators
class Activity {
    String name;
    int expectedEnrollment;
    List<String> preferredFacilitators;
    List<String> otherFacilitators;

    Activity(String name, int expectedEnrollment, List<String> preferredFacilitators, List<String> otherFacilitators) {
        this.name = name;
        this.expectedEnrollment = expectedEnrollment;
        this.preferredFacilitators = preferredFacilitators;
        this.otherFacilitators = otherFacilitators;
    }
}

// Creates the Schedule class with holds room, time, and facilitator for each Activity
class Schedule {
    Map<Activity, Room> activityRooms;
    Map<Activity, Integer> activityTimes;
    Map<Activity, String> activityFacilitators;
    double fitness;

    Schedule() {
        this.activityRooms = new HashMap<>();
        this.activityTimes = new HashMap<>();
        this.activityFacilitators = new HashMap<>();
        this.fitness = 0;
    }

// Method used to calculate fitness, or efficiency, of each schedule by a set determination of rules provided in the project outline
    double calculateFitness(List<Activity> activities, List<Room> rooms, List<String> facilitators) {
        fitness = 0;
        for (Activity activity : activities) {
            double activityFitness = 0;
            Room room = activityRooms.get(activity);
            int time = activityTimes.get(activity);
            String facilitator = activityFacilitators.get(activity);

            // Checks the room and time for conflicts
            for (Activity otherActivity : activities) {
                if (otherActivity != activity && activityTimes.get(otherActivity) == time && activityRooms.get(otherActivity) == room) {
                    activityFitness -= 0.5;
                }
            }

            // Checks for room size efficiency
            if (room.capacity < activity.expectedEnrollment) {
                activityFitness -= 0.5;
            } else if (room.capacity > activity.expectedEnrollment * 3) {
                activityFitness -= 0.2;
            } else if (room.capacity > activity.expectedEnrollment * 6) {
                activityFitness -= 0.4;
            } else {
                activityFitness += 0.3;
            }

            // Checks facilitator preferences
            if (activity.preferredFacilitators.contains(facilitator)) {
                activityFitness += 0.5;
            } else if (activity.otherFacilitators.contains(facilitator)) {
                activityFitness += 0.2;
            } else {
                activityFitness -= 0.1;
            }

            // Checks facilitator loads
            int facilitatorActivities = 0;
            for (Map.Entry<Activity, String> entry : activityFacilitators.entrySet()) {
                if (entry.getValue().equals(facilitator)) {
                    facilitatorActivities++;
                }
            }
            if (facilitatorActivities == 1) {
                activityFitness += 0.2;
            } else if (facilitatorActivities > 1 && activityTimes.get(activity) == time) {
                activityFitness -= 0.2;
            }
            if (facilitatorActivities > 4) {
                activityFitness -= 0.5;
            } else if (facilitatorActivities == 1 || facilitatorActivities == 2) {
                if (!facilitator.equals("Tyler")) {
                    activityFitness -= 0.4;
                }
            }

            // Checks for consecutive time slots
            for (Activity otherActivity : activities) {
                if (otherActivity != activity) {
                    int otherTime = activityTimes.get(otherActivity);
                    if (Math.abs(time - otherTime) == 1) {
                        if ((activity.name.equals("SLA191A") || activity.name.equals("SLA191B")) &&
                                (otherActivity.name.equals("SLA101A") || otherActivity.name.equals("SLA101B"))) {
                            activityFitness += 0.5;
                            if ((activityRooms.get(activity).name.equals("Roman") || activityRooms.get(activity).name.equals("Beach")) &&
                                    (activityRooms.get(otherActivity).name.equals("Roman") || activityRooms.get(otherActivity).name.equals("Beach"))) {
                                activityFitness -= 0.4;
                            }
                        } else if ((activity.name.equals("SLA101A") || activity.name.equals("SLA101B")) &&
                                (otherActivity.name.equals("SLA191A") || otherActivity.name.equals("SLA191B"))) {
                            activityFitness += 0.5;
                            if ((activityRooms.get(activity).name.equals("Roman") || activityRooms.get(activity).name.equals("Beach")) &&
                                    (activityRooms.get(otherActivity).name.equals("Roman") || activityRooms.get(otherActivity).name.equals("Beach"))) {
                                activityFitness -= 0.4;
                            }
                        }
                    }
                }
            }

            if (activity.name.equals("SLA101A") || activity.name.equals("SLA101B")) {
                int otherSectionTime = activityTimes.get(activities.get(activities.indexOf(activity) == 0 ? 1 : 0));
                if (Math.abs(time - otherSectionTime) > 4) {
                    activityFitness += 0.5;
                } else if (time == otherSectionTime) {
                    activityFitness -= 0.5;
                }
            } else if (activity.name.equals("SLA191A") || activity.name.equals("SLA191B")) {
                int otherSectionTime = activityTimes.get(activities.get(activities.indexOf(activity) == 2 ? 3 : 2));
                if (Math.abs(time - otherSectionTime) > 4) {
                    activityFitness += 0.5;
                } else if (time == otherSectionTime) {
                    activityFitness -= 0.5;
                }
            }

            if ((activity.name.equals("SLA191A") || activity.name.equals("SLA191B")) &&
                    (activityTimes.get(activity) == activityTimes.get(activities.get(0)) ||
                            activityTimes.get(activity) == activityTimes.get(activities.get(1)))) {
                activityFitness -= 0.25;
            } else if ((activity.name.equals("SLA101A") || activity.name.equals("SLA101B")) &&
                    (activityTimes.get(activity) == activityTimes.get(activities.get(2)) ||
                            activityTimes.get(activity) == activityTimes.get(activities.get(3)))) {
                activityFitness -= 0.25;
            }

            if (Math.abs(activityTimes.get(activity) - activityTimes.get(activities.get(0))) == 1 ||
                    Math.abs(activityTimes.get(activity) - activityTimes.get(activities.get(1))) == 1) {
                if ((activity.name.equals("SLA191A") || activity.name.equals("SLA191B"))) {
                    activityFitness += 0.25;
                }
            } else if (Math.abs(activityTimes.get(activity) - activityTimes.get(activities.get(2))) == 1 ||
                    Math.abs(activityTimes.get(activity) - activityTimes.get(activities.get(3))) == 1) {
                if ((activity.name.equals("SLA101A") || activity.name.equals("SLA101B"))) {
                    activityFitness += 0.25;
                }
            }

            fitness += activityFitness;
        }

        return fitness;
    }
}

// Creates the Room class which stores name and capacity
class Room {
    String name;
    int capacity;

    Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }
}

// Creates the GeneticScheduler class which stores population, generations, and mutations
public class GeneticScheduler {
    private static final int POPULATION_SIZE = 500;
    private static final int NUM_GENERATIONS = 100;
    private static final double MUTATION_RATE = 0.01;
    private static final List<String> FACILITATORS = Arrays.asList("Lock", "Glen", "Banks", "Richards", "Shaw", "Singer", "Uther", "Tyler", "Numen", "Zeldin");
    private static final List<Room> ROOMS = Arrays.asList(
            new Room("Slater 003", 45),
            new Room("Roman 216", 30),
            new Room("Loft 206", 75),
            new Room("Roman 201", 50),
            new Room("Loft 310", 108),
            new Room("Beach 201", 60),
            new Room("Beach 301", 75),
            new Room("Logos 325", 450),
            new Room("Frank 119", 60)
    );
    private static final List<Integer> TIMES = Arrays.asList(10, 11, 12, 13, 14, 15);
    private static final List<Activity> ACTIVITIES = Arrays.asList(
            new Activity("SLA100A", 50, Arrays.asList("Glen", "Lock", "Banks", "Zeldin"), Arrays.asList("Numen", "Richards")),
            new Activity("SLA100B", 50, Arrays.asList("Glen", "Lock", "Banks", "Zeldin"), Arrays.asList("Numen", "Richards")),
            new Activity("SLA191A", 50, Arrays.asList("Glen", "Lock", "Banks", "Zeldin"), Arrays.asList("Numen", "Richards")),
            new Activity("SLA191B", 50, Arrays.asList("Glen", "Lock", "Banks", "Zeldin"), Arrays.asList("Numen", "Richards")),
            new Activity("SLA201", 50, Arrays.asList("Glen", "Banks", "Zeldin", "Shaw"), Arrays.asList("Numen", "Richards", "Singer")),
            new Activity("SLA291", 50, Arrays.asList("Lock", "Banks", "Zeldin", "Singer"), Arrays.asList("Numen", "Richards", "Shaw", "Tyler")),
            new Activity("SLA303", 60, Arrays.asList("Glen", "Zeldin", "Banks"), Arrays.asList("Numen", "Singer", "Shaw")),
            new Activity("SLA304", 25, Arrays.asList("Glen", "Banks", "Tyler"), Arrays.asList("Numen", "Singer", "Shaw", "Richards", "Uther", "Zeldin")),
            new Activity("SLA394", 20, Arrays.asList("Tyler", "Singer"), Arrays.asList("Richards", "Zeldin")),
            new Activity("SLA449", 60, Arrays.asList("Tyler", "Singer", "Shaw"), Arrays.asList("Zeldin", "Uther")),
            new Activity("SLA451", 100, Arrays.asList("Tyler", "Singer", "Shaw"), Arrays.asList("Zeldin", "Uther", "Richards", "Banks"))
    );

// Runs the genetic algorithm
    public static void main(String[] args) {
        List<Schedule> population = initializePopulation();
        double bestFitness = Double.NEGATIVE_INFINITY;
        Schedule bestSchedule = null;

        // For loop that goes through each generation
        for (int generation = 0; generation < NUM_GENERATIONS; generation++) {
            List<Schedule> newPopulation = new ArrayList<>();

            // Selection and reproduction
            while (newPopulation.size() < POPULATION_SIZE) {
                Schedule parent1 = tournamentSelection(population);
                Schedule parent2 = tournamentSelection(population);
                Schedule offspring = crossover(parent1, parent2);
                mutate(offspring);
                newPopulation.add(offspring);
            }

            // Evaluates fitness and updates best solution
            for (Schedule schedule : newPopulation) {
                schedule.fitness = schedule.calculateFitness(ACTIVITIES, ROOMS, FACILITATORS);
                if (schedule.fitness > bestFitness) {
                    bestFitness = schedule.fitness;
                    bestSchedule = schedule;
                }
            }

            population = newPopulation;

            // Checks for termination condition
            if (generation > 100 && averageFitness(population) < 1.01 * bestFitness) {
                break;
            }
        }

        System.out.println("Best fitness: " + bestFitness);
        printSchedule(bestSchedule);
    }

    private static List<Schedule> initializePopulation() {
        List<Schedule> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Schedule schedule = new Schedule();
            for (Activity activity : ACTIVITIES) {
                Room room = ROOMS.get(new Random().nextInt(ROOMS.size()));
                int time = TIMES.get(new Random().nextInt(TIMES.size()));
                String facilitator = FACILITATORS.get(new Random().nextInt(FACILITATORS.size()));
                schedule.activityRooms.put(activity, room);
                schedule.activityTimes.put(activity, time);
                schedule.activityFacilitators.put(activity, facilitator);
            }
            population.add(schedule);
        }
        return population;
    }

    // Tournament selection for the List Schedule
    private static Schedule tournamentSelection(List<Schedule> population) {
        Schedule bestSchedule = null;
        double bestFitness = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 5; i++) {
            Schedule schedule = population.get(new Random().nextInt(population.size()));
            if (schedule.fitness > bestFitness) {
                bestFitness = schedule.fitness;
                bestSchedule = schedule;
            }
        }
        return bestSchedule;
    }

    // Determines cross over between parent 1 and parent 2
    private static Schedule crossover(Schedule parent1, Schedule parent2) {
        Schedule offspring = new Schedule();
        for (Activity activity : ACTIVITIES) {
            if (new Random().nextBoolean()) {
                offspring.activityRooms.put(activity, parent1.activityRooms.get(activity));
                offspring.activityTimes.put(activity, parent1.activityTimes.get(activity));
                offspring.activityFacilitators.put(activity, parent1.activityFacilitators.get(activity));
            } else {
                offspring.activityRooms.put(activity, parent2.activityRooms.get(activity));
                offspring.activityTimes.put(activity, parent2.activityTimes.get(activity));
                offspring.activityFacilitators.put(activity, parent2.activityFacilitators.get(activity));
            }
        }
        return offspring;
    }

    // Mutates based off of the muatation rate
    private static void mutate(Schedule schedule) {
        for (Activity activity : ACTIVITIES) {
            if (Math.random() < MUTATION_RATE) {
                schedule.activityRooms.put(activity, ROOMS.get(new Random().nextInt(ROOMS.size())));
            }
            if (Math.random() < MUTATION_RATE) {
                schedule.activityTimes.put(activity, TIMES.get(new Random().nextInt(TIMES.size())));
            }
            if (Math.random() < MUTATION_RATE) {
                schedule.activityFacilitators.put(activity, FACILITATORS.get(new Random().nextInt(FACILITATORS.size())));
            }
        }
    }

    // Calculates average fitness scores of the population size
    private static double averageFitness(List<Schedule> population) {
        double totalFitness = 0;
        for (Schedule schedule : population) {
            totalFitness += schedule.fitness;
        }
        return totalFitness / population.size();
    }

    // Parses and prints information into final schedule
    private static void printSchedule(Schedule schedule) {
        System.out.println("Schedule:");
        for (Activity activity : ACTIVITIES) {
            Room room = schedule.activityRooms.get(activity);
            int time = schedule.activityTimes.get(activity);
            String facilitator = schedule.activityFacilitators.get(activity);
            System.out.println(activity.name + " - Room: " + room.name + ", Time: " + time + ", Facilitator: " + facilitator);
        }
    }
}
