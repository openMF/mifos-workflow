package org.mifos.workflow.service.fineract.loan;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mifos.fineract.client.models.DeleteLoansLoanIdResponse;
import org.mifos.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.mifos.fineract.client.models.GetLoansLoanIdResponse;
import org.mifos.fineract.client.models.GetLoansResponse;
import org.mifos.fineract.client.models.GetLoansTemplateResponse;
import org.mifos.fineract.client.models.GetLoansApprovalTemplateResponse;
import org.mifos.fineract.client.models.PostLoansDelinquencyActionResponse;
import org.mifos.fineract.client.models.PostLoansLoanIdResponse;
import org.mifos.fineract.client.models.PostLoansResponse;
import org.mifos.fineract.client.models.PutLoansLoanIdResponse;
import org.mifos.workflow.api.loan.LoansApi;
import org.mifos.workflow.config.FineractApiConfig;
import org.mifos.workflow.util.FineractErrorHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for handling loan-related operations with the Fineract API.
 * Provides methods for loan creation, approval, disbursement, and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FineractLoanService {

    private final LoansApi loansApi;
    private final FineractApiConfig fineractApiConfig;

    @PostConstruct
    public void init() {
        log.info("FineractLoanService initialized with LoansApi");
    }

    private <T> Observable<T> handleError(Observable<T> observable, String operation, String resourceId) {
        return observable.subscribeOn(Schedulers.io()).onErrorResumeNext(error -> Observable.error(FineractErrorHandler.handleError(operation, error, resourceId)));
    }


    public PostLoansResponse createLoan(Map<String, Object> request, String command) {
        log.info("Creating loan with command: {}", command);

        return handleError(loansApi.calculateLoanScheduleOrSubmitLoanApplication(request, command), "loan creation", command).blockingFirst();
    }


    public GetLoansLoanIdResponse getLoan(Long loanId, Boolean staffInSelectedOfficeOnly, String associations, String exclude, String fields) {
        log.info("Retrieving loan with ID: {}", loanId);

        return handleError(loansApi.retrieveLoan(loanId, staffInSelectedOfficeOnly, associations, exclude, fields), "loan retrieval", loanId.toString()).blockingFirst();
    }


    public GetLoansLoanIdResponse getLoanByExternalId(String loanExternalId, Boolean staffInSelectedOfficeOnly, String associations, String exclude, String fields) {
        log.info("Retrieving loan with external ID: {}", loanExternalId);

        return handleError(loansApi.retrieveLoanByExternalId(loanExternalId, staffInSelectedOfficeOnly, associations, exclude, fields), "loan retrieval by external ID", loanExternalId).blockingFirst();
    }


    public PostLoansLoanIdResponse performStateTransition(Long loanId, Map<String, Object> request, String command) {
        log.info("Performing state transition for loan {} with command: {}", loanId, command);

        return handleError(loansApi.stateTransitions(loanId, request, command), "loan state transition", loanId + ":" + command).blockingFirst();
    }


    public PostLoansLoanIdResponse performStateTransitionByExternalId(String loanExternalId, Map<String, Object> request, String command) {
        log.info("Performing state transition for loan with external ID {} with command: {}", loanExternalId, command);

        return handleError(loansApi.stateTransitionsByExternalId(loanExternalId, request, command), "loan state transition by external ID", loanExternalId + ":" + command).blockingFirst();
    }


    public PutLoansLoanIdResponse modifyLoan(Long loanId, Map<String, Object> request, String command) {
        log.info("Modifying loan {} with command: {}", loanId, command);

        return handleError(loansApi.modifyLoanApplication(loanId, request, command), "loan modification", loanId + ":" + (command != null ? command : "no-command")).blockingFirst();
    }


    public PutLoansLoanIdResponse modifyLoanByExternalId(String loanExternalId, Map<String, Object> request, String command) {
        log.info("Modifying loan with external ID {} with command: {}", loanExternalId, command);

        return handleError(loansApi.modifyLoanApplicationByExternalId(loanExternalId, request, command), "loan modification by external ID", loanExternalId + ":" + (command != null ? command : "no-command")).blockingFirst();
    }


    public DeleteLoansLoanIdResponse deleteLoan(Long loanId) {
        log.info("Deleting loan: {}", loanId);

        return handleError(loansApi.deleteLoanApplication(loanId), "loan deletion", loanId.toString()).blockingFirst();
    }


    public DeleteLoansLoanIdResponse deleteLoanByExternalId(String loanExternalId) {
        log.info("Deleting loan with external ID: {}", loanExternalId);

        return handleError(loansApi.deleteLoanApplicationByExternalId(loanExternalId), "loan deletion by external ID", loanExternalId).blockingFirst();
    }


    public GetLoansResponse getAllLoans(String externalId, Integer offset, Integer limit, String orderBy, String sortOrder, String accountNo, String status) {
        log.info("Retrieving all loans with filters - externalId: {}, status: {}", externalId, status);

        return handleError(loansApi.retrieveAllLoans(externalId, offset, limit, orderBy, sortOrder, accountNo, status), "all loans retrieval", "filters").blockingFirst();
    }


    public GetLoansTemplateResponse getLoanTemplate(Long officeId, Long staffId, String dateFormat) {
        log.info("Getting loan template for office: {}, staff: {}", officeId, staffId);

        return handleError(loansApi.getLoansTemplate(officeId, staffId, dateFormat), "loan template retrieval", officeId + ":" + staffId).blockingFirst();
    }

    public GetLoansTemplateResponse getLoanTemplateByClient(Long clientId, Long groupId, Long productId, String templateType, Boolean staffInSelectedOfficeOnly, Boolean activeOnly) {
        log.info("Getting loan template for client: {}, group: {}, product: {}", clientId, groupId, productId);

        return handleError(loansApi.template10(clientId, groupId, productId, templateType, staffInSelectedOfficeOnly, activeOnly), "loan template retrieval by client", clientId.toString()).blockingFirst();
    }

    public GetLoansApprovalTemplateResponse getApprovalTemplate(Long loanId, String templateType) {
        log.info("Getting approval template for loan: {} with type: {}", loanId, templateType);

        return handleError(loansApi.retrieveApprovalTemplate(loanId, templateType), "approval template retrieval", loanId.toString()).blockingFirst();
    }

    public GetLoansApprovalTemplateResponse getApprovalTemplateByExternalId(String loanExternalId, String templateType) {
        log.info("Getting approval template for loan with external ID: {} with type: {}", loanExternalId, templateType);

        return handleError(loansApi.retrieveApprovalTemplateByExternalId(loanExternalId, templateType), "approval template retrieval by external ID", loanExternalId).blockingFirst();
    }

    public String postLoanTemplate(String dateFormat, String locale, Object uploadedInputStream) {
        log.info("Posting loan template with dateFormat: {}, locale: {}", dateFormat, locale);

        return handleError(loansApi.postLoanTemplate(dateFormat, locale, uploadedInputStream), "loan template posting", "template").blockingFirst();
    }

    public String postLoanRepaymentTemplate(String dateFormat, String locale, Object uploadedInputStream) {
        log.info("Posting loan repayment template with dateFormat: {}, locale: {}", dateFormat, locale);

        return handleError(loansApi.postLoanRepaymentTemplate(dateFormat, locale, uploadedInputStream), "loan repayment template posting", "repayment-template").blockingFirst();
    }

    public void getLoanRepaymentTemplate(Long officeId, String dateFormat) {
        log.info("Getting loan repayment template for office: {}", officeId);

        handleError(loansApi.getLoanRepaymentTemplate(officeId, dateFormat), "loan repayment template retrieval", officeId.toString()).blockingFirst();
    }

    public PostLoansLoanIdResponse performGlimStateTransition(Long glimId, Map<String, Object> request, String command) {
        log.info("Performing GLIM state transition for GLIM {} with command: {}", glimId, command);

        return handleError(loansApi.glimStateTransitions(glimId, request, command), "GLIM state transition", glimId + ":" + command).blockingFirst();
    }

    public String getGlimRepaymentTemplate(Long glimId) {
        log.info("Getting GLIM repayment template for GLIM: {}", glimId);

        return handleError(loansApi.getGlimRepaymentTemplate(glimId), "GLIM repayment template retrieval", glimId.toString()).blockingFirst();
    }

    public GetLoansLoanIdResponse getLoanWithAssociations(Long loanId, String associations) {
        log.info("Retrieving loan {} with associations: {}", loanId, associations);

        return handleError(loansApi.retrieveLoan(loanId, null, associations, null, null), "loan retrieval with associations", loanId.toString()).blockingFirst();
    }

    public GetLoansLoanIdResponse getLoanWithFields(Long loanId, String fields) {
        log.info("Retrieving loan {} with fields: {}", loanId, fields);

        return handleError(loansApi.retrieveLoan(loanId, null, null, null, fields), "loan retrieval with fields", loanId.toString()).blockingFirst();
    }

    public GetLoansLoanIdResponse getLoanByExternalIdWithAssociations(String loanExternalId, String associations) {
        log.info("Retrieving loan with external ID {} with associations: {}", loanExternalId, associations);

        return handleError(loansApi.retrieveLoanByExternalId(loanExternalId, null, associations, null, null), "loan retrieval by external ID with associations", loanExternalId).blockingFirst();
    }

    public GetLoansLoanIdResponse getLoanByExternalIdWithFields(String loanExternalId, String fields) {
        log.info("Retrieving loan with external ID {} with fields: {}", loanExternalId, fields);

        return handleError(loansApi.retrieveLoanByExternalId(loanExternalId, null, null, null, fields), "loan retrieval by external ID with fields", loanExternalId).blockingFirst();
    }

    public List<GetDelinquencyTagHistoryResponse> getDelinquencyTagHistory(Long loanId) {
        log.info("Getting delinquency tag history for loan: {}", loanId);

        return handleError(loansApi.getDelinquencyTagHistory(loanId), "delinquency tag history retrieval", loanId.toString()).blockingFirst();
    }


    public PostLoansDelinquencyActionResponse createDelinquencyAction(Long loanId, Map<String, Object> request) {
        log.info("Creating delinquency action for loan: {}", loanId);

        return handleError(loansApi.createLoanDelinquencyAction(loanId, request), "delinquency action creation", loanId.toString()).blockingFirst();
    }
}
