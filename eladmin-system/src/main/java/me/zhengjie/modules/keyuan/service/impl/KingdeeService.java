package me.zhengjie.modules.keyuan.service.impl;


import com.kingdee.service.ApiClient;
import com.kingdee.service.ApiException;
import com.kingdee.service.Configuration;
import com.kingdee.service.data.api.AccountBookApi;
import com.kingdee.service.data.api.AppTokenApi;
import com.kingdee.service.data.api.AuthorizeApi;
import com.kingdee.service.data.entity.AccountBalanceReply;
import com.kingdee.service.data.entity.AccountBalanceReplyRow;
import com.kingdee.service.data.entity.AccountBookAccountBalanceReq;
import com.kingdee.service.data.entity.AsterAppTokenRes;
import com.kingdee.service.data.entity.AsterAuthorizeRes;
import com.kingdee.service.unit.SHAUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.domain.balance.AccountBalanceData;
import me.zhengjie.modules.keyuan.utils.KingdeeUtils;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KingdeeService {
    @Value("${kingdee.client_id}")
    private String clientId;

    @Value("${kingdee.client_secret}")
    private String clientSecret;

    @Value("${kingdee.instance_id}")
    private String instanceId;

    private Map<String, AccountBalanceReplyRow> balanceReplyRowMap = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("KingdeeService init start");
        try {
            ApiClient defaultApiClient = Configuration.getDefaultApiClient();
            defaultApiClient.setClientId(clientId);
            defaultApiClient.setClientSecret(clientSecret);
            refreshToken();
            refreshBalanceRowCache();
            log.info("KingdeeService init success");
        } catch (Exception e) {
            log.error("KingdeeService init error", e);
        }
    }

    public void refreshToken() throws ApiException {
        AuthorizeApi authorizeApi = new AuthorizeApi();
        List<AsterAuthorizeRes> asterAuthorizeResList = authorizeApi.asterAuthorize(instanceId);
        AppTokenApi appTokenApi = new AppTokenApi();
        String appSignature = SHAUtil.SHA256HMAC(asterAuthorizeResList.get(0).getAppKey(), asterAuthorizeResList.get(0).getAppSecret());
        appSignature = Base64.getEncoder().encodeToString(appSignature.getBytes());
        AsterAppTokenRes asterAppTokenRes = appTokenApi.asterAppToken(asterAuthorizeResList.get(0).getAppKey(), appSignature, null);
        Configuration.getDefaultApiClient().setAppToken(asterAppTokenRes.getAppToken());
    }

    public void refreshBalanceRowCache() throws ApiException {
        AccountBookApi accountBookApi = new AccountBookApi();
        AccountBookAccountBalanceReq req = new AccountBookAccountBalanceReq()
                .endPeriod(KingdeeUtils.getThisMonthPeriod())
                .startPeriod(KingdeeUtils.getThisMonthPeriod());

        AccountBalanceReply accountBalanceReply = accountBookApi.accountBookAccountBalance(req);
        balanceReplyRowMap = accountBalanceReply.getRows().stream().collect(Collectors.toMap(
                AccountBalanceReplyRow::getAccountNo,
                Function.identity(),
                (x, y) -> x
        ));
        balanceReplyRowMap.remove("");
    }

    public AccountBalanceData getAccountBalanceByNumber(String accountNumber) {
        AccountBalanceData data = new AccountBalanceData();
        AccountBalanceReplyRow row = balanceReplyRowMap.get(accountNumber);
        if (row == null) {
            log.info("null AccountBalanceReplyRow accountNumber = " + accountNumber);
            return data;
        }
        data.setExpenseLast(ProjectUtils.realPriceToDbPrice(row.getBeginBal()));
        data.setExpenseThisMonth(ProjectUtils.realPriceToDbPrice(row.getDebit()));
        data.setExpenseThisYear(ProjectUtils.realPriceToDbPrice(row.getYtdDebit()));
        return data;
    }
}
