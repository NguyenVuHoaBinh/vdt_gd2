package Viettel.backend.agentState;

import org.springframework.stereotype.Service;

@Service
public class StateService {
    private TaskState currentState;

    public TaskState getCurrentState(){
        return currentState;
    }

    public void setCurrentState(TaskState currentState) {
        this.currentState = currentState;
        System.out.println("Current state set to: " + currentState);
    }

    public void resetState(){
        this.currentState = null;
        System.out.println("State has been reset");
    }


}
