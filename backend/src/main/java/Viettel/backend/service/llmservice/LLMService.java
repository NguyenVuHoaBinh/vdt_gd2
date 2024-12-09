package Viettel.backend.service.llmservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class LLMService {
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    private final SQLOpenAiService sqlOpenAiService;
    private final SQLGeminiService sqlGeminiService;

    @Autowired
    public LLMService(SQLOpenAiService sqlOpenAiService, SQLGeminiService sqlGeminiService) {
        this.sqlOpenAiService = sqlOpenAiService;
        this.sqlGeminiService = sqlGeminiService;
    }

    public Map<String, String> processMessage(String message, String model, String role) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processMessage(message, role);
            case "gemini":
                return sqlGeminiService.processMessage(message, role);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processAnalysis(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processAnalysis(message);
            case "gemini":
                return sqlGeminiService.processAnalysis(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String generateRefinedQuery(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.generateRefinedQuery(message);
            case "gemini":
                return sqlGeminiService.generateRefinedQuery(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String generateHyDE(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.generateHyDE(message);
            case "gemini":
                return sqlGeminiService.generateHyDE(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String errorSolver(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.errorSolver(message);
            case "gemini":
                return sqlGeminiService.errorSolver(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String create_invoice(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.create_invoice(message);
            case "gemini":
                return sqlGeminiService.create_invoice(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String create_invoice_warehouse(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.create_invoice_warehouse(message);
            case "gemini":
                return sqlGeminiService.create_invoice_warehouse(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String get_product_detail(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.get_product_detail(message);
            case "gemini":
                return sqlGeminiService.get_product_detail(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processOrder(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processOrder(message);
            case "gemini":
                return sqlGeminiService.processOrder(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public Map<String, String> createCustomerSQL(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.createCustomerSQL(message);
            case "gemini":
                return sqlGeminiService.createCustomerSQL(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String taskAnalysis(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.task_analysis(message);
            case "gemini":
                return sqlGeminiService.task_analysis(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String checkCustomer(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.checkCustomer(message);
            case "gemini":
                return sqlGeminiService.checkCustomer(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String isOrder(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.isOrder(message);
            case "gemini":
                return sqlGeminiService.isOrder(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String isCustomerInfo(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.isCustomerInfo(message);
            case "gemini":
                return sqlGeminiService.isCustomerInfo(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String isInvoice(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.isInvoice(message);
            case "gemini":
                return sqlGeminiService.isInvoice(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String isContainSQL(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.isContainSQL(message);
            case "gemini":
                return sqlGeminiService.isContainSQL(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processPayment(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processPayment(message);
            case "gemini":
                return sqlGeminiService.processPayment(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String createInvoiceSQL(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.createInvoiceSQL(message);
            case "gemini":
                return sqlGeminiService.createInvoiceSQL(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String createInvoiceDetail(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.createInvoiceDetail(message);
            case "gemini":
                return sqlGeminiService.createInvoiceDetail(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public Map<String, String> createImmediatelyPayment(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.createImmediatelyPayment(message);
            case "gemini":
                return sqlGeminiService.createImmediatelyPayment(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }



    public String updateStock(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.updateStock(message);
            case "gemini":
                return sqlGeminiService.updateStock(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public Map<String, String> createDebtPayment(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.createDebtPayment(message);
            case "gemini":
                return sqlGeminiService.createDebtPayment(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String updatePaidInvoiceSQL(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.updatePaidInvoiceSQL(message);
            case "gemini":
                return sqlGeminiService.updatePaidInvoiceSQL(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String updateDeferredInvoiceSQL(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.updateDeferredInvoiceSQL(message);
            case "gemini":
                return sqlGeminiService.updateDeferredInvoiceSQL(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String updateCancelledInvoiceSQL(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.updateCancelledInvoiceSQL(message);
            case "gemini":
                return sqlGeminiService.updateCancelledInvoiceSQL(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String checkDebt(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.checkDebt(message);
            case "gemini":
                return sqlGeminiService.checkDebt(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processCheckDebt(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processCheckDebt(message);
            case "gemini":
                return sqlGeminiService.processCheckDebt(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String stockAlert(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.stockAlert(message);
            case "gemini":
                return sqlGeminiService.stockAlert(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }


    public Map<String, String> processImport(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processImport(message);
            case "gemini":
                return sqlGeminiService.processImport(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public Map<String, String> adjustProduct(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.adjustProduct(message);
            case "gemini":
                return sqlGeminiService.adjustProduct(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String updateLastShopping(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.updateLastShopping(message);
            case "gemini":
                return sqlGeminiService.updateLastShopping(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String checkCustomerShoppingActivity(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.checkCustomerShoppingActivity(message);
            case "gemini":
                return sqlGeminiService.checkCustomerShoppingActivity(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String sellingTrend(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.sellingTrend(message);
            case "gemini":
                return sqlGeminiService.sellingTrend(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String businessAnalysis(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.businessAnalysis(message);
            case "gemini":
                return sqlGeminiService.businessAnalysis(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processBusinessAnalysis(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processBusinessAnalysis(message);
            case "gemini":
                return sqlGeminiService.processBusinessAnalysis(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }



    public String processCheckCustomerShoppingActivity(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processCheckCustomerShoppingActivity(message);
            case "gemini":
                return sqlGeminiService.processCheckCustomerShoppingActivity(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String productTrending(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.productTrending(message);
            case "gemini":
                return sqlGeminiService.productTrending(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String processProductTrending(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.processProductTrending(message);
            case "gemini":
                return sqlGeminiService.processProductTrending(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String audioSmoothTranslation(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.audioSmoothTranslation(message);
            case "gemini":
                return sqlGeminiService.audioSmoothTranslation(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String jsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.jsonConverter(message);
            case "gemini":
                return sqlGeminiService.jsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String importJsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.importJsonConverter(message);
            case "gemini":
                return sqlGeminiService.importJsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }

    }

    public String baJsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.baJsonConverter(message);
            case "gemini":
                return sqlGeminiService.baJsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String debtJsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.debtJsonConverter(message);
            case "gemini":
                return sqlGeminiService.debtJsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String customerInactiveJsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.customerInactiveJsonConverter(message);
            case "gemini":
                return sqlGeminiService.customerInactiveJsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String restockJsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.restockJsonConverter(message);
            case "gemini":
                return sqlGeminiService.restockJsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String sellingTrendJsonConverter(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.sellingTrendJsonConverter(message);
            case "gemini":
                return sqlGeminiService.sellingTrendJsonConverter(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String mydioCall(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.mydioCall(message);
            case "gemini":
                return sqlGeminiService.mydioCall(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String mydioExec(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.mydioExec(message);
            case "gemini":
                return sqlGeminiService.mydioExec(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String mydioAnalysis(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.mydioAnalysis(message);
            case "gemini":
                return sqlGeminiService.mydioAnalysis(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }
    public String mydioStart(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.mydioStart(message);
            case "gemini":
                return sqlGeminiService.mydioStart(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }

    public String ambiguousDetection(String message, String model) {
        switch (model.toLowerCase()) {
            case "gpt-3":
                return sqlOpenAiService.ambiguousDetection(message);
            case "gemini":
                return sqlGeminiService.ambiguousDetection(message);
            default:
                throw new IllegalArgumentException("Unsupported model: " + model);
        }
    }


}
