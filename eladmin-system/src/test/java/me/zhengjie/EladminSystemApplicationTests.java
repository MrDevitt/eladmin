package me.zhengjie;

import com.alibaba.fastjson.JSON;
import com.kingdee.service.ApiClient;
import com.kingdee.service.ApiException;
import com.kingdee.service.data.api.AccountBookApi;
import com.kingdee.service.data.api.AppTokenApi;
import com.kingdee.service.data.api.AuthorizeApi;
import com.kingdee.service.data.entity.AccountBalanceReply;
import com.kingdee.service.data.entity.AccountBookAccountBalanceReq;
import com.kingdee.service.data.entity.AsterAppTokenRes;
import com.kingdee.service.data.entity.AsterAuthorizeRes;
import com.kingdee.service.unit.SHAUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;
import java.util.List;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EladminSystemApplicationTests {

    @Test
    public void contextLoads() {
    }

    public static void main(String[] args) throws ApiException {
        log.info("KingdeeConfig init start");
        try {
            ApiClient defaultApiClient = com.kingdee.service.Configuration.getDefaultApiClient();
            defaultApiClient.setClientId("309299");
            defaultApiClient.setClientSecret("4c9b27d21a65e5772072ca46c04df4c1");

            AuthorizeApi authorizeApi = new AuthorizeApi();
            List<AsterAuthorizeRes> asterAuthorizeResList = authorizeApi.asterAuthorize("394650056168443904");
            log.info(JSON.toJSONString(asterAuthorizeResList));
            AppTokenApi appTokenApi = new AppTokenApi();
            String appSignature = SHAUtil.SHA256HMAC(asterAuthorizeResList.get(0).getAppKey(), asterAuthorizeResList.get(0).getAppSecret());
            appSignature = Base64.getEncoder().encodeToString(appSignature.getBytes());
            AsterAppTokenRes asterAppTokenRes = appTokenApi.asterAppToken(asterAuthorizeResList.get(0).getAppKey(), appSignature, null);
            log.info(JSON.toJSONString(asterAppTokenRes));
            defaultApiClient.setAppToken(asterAppTokenRes.getAppToken());
//            defaultApiClient.setAppToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHQiOnsiYWNjb3VudElkIjoiMTc0NDA5OTAyNDkxNjA5NzIyNyIsImdyb3VwTmFtZSI6Im5zLXQ2MyIsImFwcF9rZXkiOiJPSFZIczZGWSIsInRlbmFudElkIjoiNzk1Nzg5MTU5ODcwNiIsInVzZXJOYW1lIjoidTE1MTExODUxODAwIn0sImdycCI6Im5zLXQ2MyIsImV4cCI6MTc0NDk3MDE0NywiYWlkIjoiMTc0NDA5OTAyNDkxNjA5NzIyNyIsImlhdCI6MTc0NDg4Mzc0N30.2kh2qHBxKOq-wh43jLSqzrW59Zh3y7leZyLdN5OC5GA");
            log.info("KingdeeConfig init success, token=" + asterAppTokenRes.getAppToken());
        } catch (Exception e) {
            log.error("KingdeeConfig init error", e);
        }
        AccountBookApi accountBookApi = new AccountBookApi();

        AccountBookAccountBalanceReq req = new AccountBookAccountBalanceReq()
                .endPeriod("202503")
                .startPeriod("202503");
//                .search("54010103");
        AccountBalanceReply accountBalanceReply = accountBookApi.accountBookAccountBalance(req);
        log.info(JSON.toJSONString(accountBalanceReply));

    }
}

