package Viettel.backend.agentState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskAnalysisService {

    @Autowired
    private StateService stateService;

    public void analyzeTask(String apiResponse, String message) {
        TaskState newState = null;
        switch (apiResponse) {
            case "order":
                newState = TaskState.ORDER;
                break;
            case "invoice":
                newState = TaskState.INVOICE;
                break;
            case "payment":
                newState = TaskState.PAYMENT;
                break;
            case "debt":
                newState = TaskState.DEBT;
                break;
            case "import":
                newState = TaskState.IMPORT;
                break;
            case "adjust":
                newState = TaskState.ADJUST_PRICE;
                break;
            case "check":
                newState = TaskState.CHECK_DEBT;
                break;
            default:
                System.out.println("Unknown response, no state change.");
                return;
        }
        // Set the new state and disable the others
        stateService.setCurrentState(newState);
    }
}
