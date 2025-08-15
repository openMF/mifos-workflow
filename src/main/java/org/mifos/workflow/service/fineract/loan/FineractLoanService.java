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


    public GetLoansTemplateResponse getLoanTemplate(Long officeId, Long staffId, String dateFormat) {
        log.info("Getting loan template for office: {}, staff: {}", officeId, staffId);

        return handleError(loansApi.getLoansTemplate(officeId, staffId, dateFormat), "loan template retrieval", officeId + ":" + staffId).blockingFirst();
    }


    public List<GetDelinquencyTagHistoryResponse> getDelinquencyTagHistory(Long loanId) {
        log.info("Getting delinquency tag history for loan: {}", loanId);

        return handleError(loansApi.getDelinquencyTagHistory(loanId), "delinquency tag history retrieval", loanId.toString()).blockingFirst();
    }


    public PostLoansDelinquencyActionResponse createDelinquencyAction(Long loanId, Map<String, Object> request) {
        log.info("Creating delinquency action for loan: {}", loanId);

        return handleError(loansApi.createLoanDelinquencyAction(loanId, request), "delinquency action creation", loanId.toString()).blockingFirst();
    }


    public GetLoansResponse getAllLoans(String externalId, Integer offset, Integer limit, String orderBy, String sortOrder, String accountNo, String status) {
        log.info("Retrieving all loans with filters - externalId: {}, status: {}", externalId, status);

        return handleError(loansApi.retrieveAllLoans(externalId, offset, limit, orderBy, sortOrder, accountNo, status), "all loans retrieval", "filters").blockingFirst();
    }
}
