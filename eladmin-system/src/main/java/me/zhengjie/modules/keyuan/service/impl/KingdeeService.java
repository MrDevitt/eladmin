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
import me.zhengjie.modules.keyuan.domain.statistics.balance.AccountBalanceData;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Calendar;
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

    @Value("${kingdee.enabled}")
    private boolean enabled;

    private final Map<Integer, Map<String, AccountBalanceReplyRow>> balanceReplyByMonthAndNumber = new HashMap<>();

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("KingdeeService disabled");
            return;
        }
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
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        for (int i = 1; i <= currentMonth; i++) {
            if (balanceReplyByMonthAndNumber.containsKey(i) && i != currentMonth) {
                continue;
            }
            String period = year + String.format("%02d", i);
            AccountBookAccountBalanceReq req = new AccountBookAccountBalanceReq()
                    .endPeriod(period)
                    .startPeriod(period);
            AccountBalanceReply accountBalanceReply = accountBookApi.accountBookAccountBalance(req);
            Map<String, AccountBalanceReplyRow> map = accountBalanceReply.getRows().stream().collect(Collectors.toMap(
                    AccountBalanceReplyRow::getAccountNo,
                    Function.identity(),
                    (x, y) -> x
            ));
            map.remove("");
            balanceReplyByMonthAndNumber.put(i, map);
        }
    }

    public AccountBalanceData getAccountBalanceByNumber(String accountNumber, int month) {
        AccountBalanceData data = new AccountBalanceData();
        Map<String, AccountBalanceReplyRow> map = balanceReplyByMonthAndNumber.get(month);
        if (map == null) {
            return data;
        }
        AccountBalanceReplyRow row = map.get(accountNumber);
        if (row == null) {
            return data;
        }
        data.setExpenseLast(ProjectUtils.realPriceToDbPrice(row.getBeginBal()) * Long.parseLong(row.getBeginDc()));
        data.setExpenseThisMonth(ProjectUtils.realPriceToDbPrice(row.getDebit()) - ProjectUtils.realPriceToDbPrice(row.getCredit()));
        data.setExpenseThisYear(ProjectUtils.realPriceToDbPrice(row.getEndBal()) * Long.parseLong(row.getEndDc()));
        return data;
    }

    public AccountBalanceData getAccountBalanceByNumberList(List<String> accountNumberList, int month) {
        AccountBalanceData res = new AccountBalanceData();
        if (accountNumberList == null) {
            return res;
        }
        for (String accountNumber : accountNumberList) {
            res = AccountBalanceData.add(res, getAccountBalanceByNumber(accountNumber, month));
        }
        return res;
    }
}
