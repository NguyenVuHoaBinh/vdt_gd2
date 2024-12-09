package Viettel.backend.service.llmservice;

import java.util.Map;

public interface LLMServiceInterface {
    Map<String, String> processMessage(String message, String role);
    String processAnalysis(String message);
    String generateHyDE(String message);
    String generateRefinedQuery(String message);
    String errorSolver(String message);

    String create_invoice(String message);
    String create_invoice_warehouse(String message);
    String get_product_detail(String message);

    String task_analysis(String message);
    String processOrder(String message);
    String checkCustomer(String message);
    Map<String, String> createCustomerSQL(String message);
    String isOrder(String message);
    String isCustomerInfo(String message);
    String isInvoice(String message);
    String isContainSQL(String message);
    String processPayment(String message);
    Map<String, String> createImmediatelyPayment(String message);
    String createInvoiceSQL(String message);
    String updatePaidInvoiceSQL(String message);
    String updateDeferredInvoiceSQL(String message);
    String updateCancelledInvoiceSQL(String message);
    String updateInvoiceSQL(String message);
    String createInvoiceDetail(String message);
    String updateStock(String message);
    Map<String, String> createDebtPayment(String message);

    String checkDebt(String message);
    //TODO: change to general interpret
    String processCheckDebt(String message);
    Map<String, String> processImport(String message);
    Map<String, String> adjustProduct(String message);
    String stockAlert(String message);

    String updateLastShopping(String message);
    String checkCustomerShoppingActivity(String message);
    String processCheckCustomerShoppingActivity(String message);
    String sellingTrend(String message);
    String businessAnalysis(String message);
    String processBusinessAnalysis(String message );

    String productTrending(String message);
    String processProductTrending(String message);
    String audioSmoothTranslation(String message);

    String jsonConverter(String message);
    String importJsonConverter(String message);
    String baJsonConverter(String message);
    String debtJsonConverter(String message);
    String customerInactiveJsonConverter(String message);
    String restockJsonConverter(String message);
    String sellingTrendJsonConverter(String message);

    String mydioCall(String message);
    String mydioExec(String message);
    String mydioAnalysis(String message);
    String mydioStart(String message);
    String ambiguousDetection(String message);


}
