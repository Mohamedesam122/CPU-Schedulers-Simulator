package com.mycompany.cpu_schedulers_main;

import java.util.*;

// ================= Process Class =================
class Process {
    private String name;
    private int arrivalTime, burstTime, remainingTime, priority, quantum, completionTime;

    public Process(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.quantum = quantum;
        this.completionTime = 0;
    }

    // getters & setters
    public String getName() { return name; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int remainingTime) { this.remainingTime = remainingTime; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public int getQuantum() { return quantum; }
    public void setQuantum(int quantum) { this.quantum = quantum; }
    public int getCompletionTime() { return completionTime; }
    public void setCompletionTime(int completionTime) { this.completionTime = completionTime; }
}

// ================= Scheduler Interface =================
interface Scheduler {
    void run();  // Team member implements logic here
    SchedulerResult getResult(); // return execution results
}

// ================= SchedulerResult =================
class SchedulerResult {
    private List<String> executionOrder = new ArrayList<>();
    private Map<String, Integer> waitingTimes = new HashMap<>();
    private Map<String, Integer> turnaroundTimes = new HashMap<>();
    private double averageWaitingTime;
    private double averageTurnaroundTime;
    private Map<String, List<Integer>> quantumHistory = new HashMap<>();

    // getters & setters
    public List<String> getExecutionOrder() { return executionOrder; }
    public Map<String, Integer> getWaitingTimes() { return waitingTimes; }
    public Map<String, Integer> getTurnaroundTimes() { return turnaroundTimes; }
    public double getAverageWaitingTime() { return averageWaitingTime; }
    public double getAverageTurnaroundTime() { return averageTurnaroundTime; }
    public Map<String, List<Integer>> getQuantumHistory() { return quantumHistory; }

    public void setExecutionOrder(List<String> executionOrder) { this.executionOrder = executionOrder; }
    public void setWaitingTimes(Map<String, Integer> waitingTimes) { this.waitingTimes = waitingTimes; }
    public void setTurnaroundTimes(Map<String, Integer> turnaroundTimes) { this.turnaroundTimes = turnaroundTimes; }
    public void setAverageWaitingTime(double averageWaitingTime) { this.averageWaitingTime = averageWaitingTime; }
    public void setAverageTurnaroundTime(double averageTurnaroundTime) { this.averageTurnaroundTime = averageTurnaroundTime; }
    public void setQuantumHistory(Map<String, List<Integer>> quantumHistory) { this.quantumHistory = quantumHistory; }
}

// ================= ResultCalculator =================
class ResultCalculator {
    public static SchedulerResult calculate(List<Process> processes, List<String> executionOrder,
                                            Map<String, List<Integer>> quantumHistory) {
        SchedulerResult result = new SchedulerResult();
        Map<String, Integer> waitingTimes = new HashMap<>();
        Map<String, Integer> turnaroundTimes = new HashMap<>();
        double totalWT = 0, totalTAT = 0;

        for (Process p : processes) {
            int tat = p.getCompletionTime() - p.getArrivalTime();
            int wt = tat - p.getBurstTime();
            turnaroundTimes.put(p.getName(), tat);
            waitingTimes.put(p.getName(), wt);
            totalWT += wt;
            totalTAT += tat;
        }

        result.setExecutionOrder(executionOrder);
        result.setWaitingTimes(waitingTimes);
        result.setTurnaroundTimes(turnaroundTimes);
        result.setAverageWaitingTime(totalWT / processes.size());
        result.setAverageTurnaroundTime(totalTAT / processes.size());
        result.setQuantumHistory(quantumHistory);

        return result;
    }
}

// ================= Skeleton Scheduler Classes =================

// --------- Preemptive SJF Scheduler ---------
class SJFScheduler implements Scheduler {
    private List<Process> processes;
    private int contextSwitch;
    private SchedulerResult result;
    private Map<String, List<Integer>> quantumHistory = new HashMap<>();

    public SJFScheduler(List<Process> processes, int contextSwitch) {
        this.processes = processes;
        this.contextSwitch = contextSwitch;
        for(Process p : processes){
            quantumHistory.put(p.getName(), new ArrayList<>());
        }
    }


@Override
public void run() {
    List<String> executionOrder = new ArrayList<>();

    int currentTime = 0;
    int completed = 0;
    int n = processes.size();

    Process lastProcess = null;

while (completed < n) {

    Process shortest = null;
    int minRemaining = Integer.MAX_VALUE;

    for (Process p : processes) {
        if (p.getArrivalTime() <= currentTime &&
            p.getRemainingTime() > 0 &&
            p.getRemainingTime() < minRemaining) {

            minRemaining = p.getRemainingTime();
            shortest = p;
        }
    }

    if (shortest == null) {
        currentTime++;
        continue;
    }

    // 👉 context switch
    if (lastProcess != null && lastProcess != shortest) {
        currentTime += contextSwitch;
    }

    executionOrder.add(shortest.getName());
    shortest.setRemainingTime(shortest.getRemainingTime() - 1);
    currentTime++;

    if (shortest.getRemainingTime() == 0) {
        shortest.setCompletionTime(currentTime);
        completed++;
    }

    lastProcess = shortest;
}

    result = ResultCalculator.calculate(processes, executionOrder, quantumHistory);
}


    @Override
    public SchedulerResult getResult() {
        return result;
    }
}

// --------- Round Robin Scheduler ---------
class RRScheduler implements Scheduler {
    private List<Process> processes;
    private int contextSwitchingTime;
    private SchedulerResult result;
    private Map<String, List<Integer>> quantumHistory = new HashMap<>();

    public RRScheduler(List<Process> processes, int contextSwitchingTime) {
        this.processes = processes;
        this.contextSwitchingTime = contextSwitchingTime;
        for(Process p : processes){
            quantumHistory.put(p.getName(), new ArrayList<>());
            quantumHistory.get(p.getName()).add(p.getQuantum());
        }
    }

   @Override
public void run() {
    List<String> executionOrder = new ArrayList<>();

    // ترتيب حسب arrival time
    processes.sort(Comparator.comparingInt(Process::getArrivalTime));

    Queue<Process> readyQueue = new LinkedList<>();

    int currentTime = 0;
    int completed = 0;
    int n = processes.size();
    int index = 0;

    Process lastProcess = null;

    while (completed < n) {

        
        while (index < n && processes.get(index).getArrivalTime() <= currentTime) {
            readyQueue.add(processes.get(index));
            index++;
        }

        if (readyQueue.isEmpty()) {
            currentTime++;
            continue;
        }

        Process current = readyQueue.poll();

       
        if (lastProcess != null && lastProcess != current) {
            currentTime += contextSwitchingTime;
        }

        int execTime = Math.min(current.getQuantum(), current.getRemainingTime());

       
        for (int i = 0; i < execTime; i++) {
            executionOrder.add(current.getName());
            current.setRemainingTime(current.getRemainingTime() - 1);
            currentTime++;

          
            while (index < n && processes.get(index).getArrivalTime() <= currentTime) {
                readyQueue.add(processes.get(index));
                index++;
            }

            if (current.getRemainingTime() == 0)
                break;
        }

       
        if (current.getRemainingTime() == 0) {
            current.setCompletionTime(currentTime);
            completed++;
        } else {
            readyQueue.add(current);
        }

        lastProcess = current;
    }

    result = ResultCalculator.calculate(processes, executionOrder, quantumHistory);
}


    @Override
    public SchedulerResult getResult() {
        return result;
    }
}

// --------- Priority Scheduler ---------
class PriorityScheduler implements Scheduler {
    private List<Process> processes;
    private SchedulerResult result;
    private Map<String, List<Integer>> quantumHistory = new HashMap<>();

    public PriorityScheduler(List<Process> processes) {
        this.processes = processes;
        for(Process p : processes){
            quantumHistory.put(p.getName(), new ArrayList<>());
        }
    }

    @Override
    public void run() {
        List<String> executionOrder = new ArrayList<>();
        // TODO: Implement Priority Scheduler logic here
        // Update:
        // - remainingTime
        // - completionTime
        // - executionOrder
        // - quantumHistory if Quantum changes
        result = ResultCalculator.calculate(processes, executionOrder, quantumHistory);
    }

    @Override
    public SchedulerResult getResult() {
        return result;
    }
}

// --------- AG Scheduler ---------
class AGScheduler implements Scheduler {
    private List<Process> processes;
    private SchedulerResult result;
    private Map<String, List<Integer>> quantumHistory = new HashMap<>();

    public AGScheduler(List<Process> processes) {
        this.processes = processes;
        for(Process p : processes){
            quantumHistory.put(p.getName(), new ArrayList<>());
            quantumHistory.get(p.getName()).add(p.getQuantum());
        }
    }

    @Override
    public void run() {
        List<String> executionOrder = new ArrayList<>();
        // TODO: Implement AG Scheduler logic here
        // Update:
        // - remainingTime
        // - completionTime
        // - executionOrder
        // - quantumHistory according to AG rules
        result = ResultCalculator.calculate(processes, executionOrder, quantumHistory);
    }

    @Override
    public SchedulerResult getResult() {
        return result;
    }
}

// ================= Main for User Input =================
public class CPU_Schedulers_Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        List<Process> processes = new ArrayList<>();

        // ===== Inputs زي test case الدكتور =====
        System.out.print("Enter Context Switching Time: ");
        int contextSwitch = sc.nextInt();

        System.out.print("Enter rrQuantum: ");
        int rrQuantum = sc.nextInt();

        System.out.print("Enter agingInterval: ");
        int agingInterval = sc.nextInt();

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) {
            System.out.println("Process " + (i + 1));

            System.out.print("Name: ");
            String name = sc.nextLine();

            System.out.print("Arrival Time: ");
            int at = sc.nextInt();

            System.out.print("Burst Time: ");
            int bt = sc.nextInt();

            System.out.print("Priority: ");
            int pr = sc.nextInt();

            sc.nextLine(); // consume newline

           
            processes.add(new Process(name, at, bt, pr, rrQuantum));
        }

        Scheduler scheduler = new RRScheduler(processes, contextSwitch);

        scheduler.run();

        SchedulerResult result = scheduler.getResult();

        // ===== Output =====
        System.out.println("\nExecution Order:");
        System.out.println(result.getExecutionOrder());

        System.out.println("\nWaiting Times:");
        System.out.println(result.getWaitingTimes());

        System.out.println("\nTurnaround Times:");
        System.out.println(result.getTurnaroundTimes());

        System.out.println("\nAverage Waiting Time: " + result.getAverageWaitingTime());
        System.out.println("Average Turnaround Time: " + result.getAverageTurnaroundTime());
    }
}

