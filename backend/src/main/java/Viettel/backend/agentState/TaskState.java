package Viettel.backend.agentState;

public enum TaskState {
    ORDER,          // Responsible for handling generating order
    INVOICE,        // Responsible for handling generating invoice
    PAYMENT,        // Responsible for handling generating payment
    DEBT,           // Responsible for handling customer debt
    IMPORT,         // Responsible for handling import good
    ADJUST_PRICE,   // Responsible for handling adjust price of good
    CHECK_DEBT      // Responsible for checking all available debt
}
