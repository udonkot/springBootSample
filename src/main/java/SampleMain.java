import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@EnableAutoConfiguration
public class SampleMain {

    // トークンID取得
    private static final String SLACK_TOKEN = System.getenv("SLACK_TOKEN");
    // チャンネルID取得
    private static final String THANKS_CHANNEL = System.getenv("THANKS_CHANNEL");

    // slackインスタンス作成
    Slack slack = Slack.getInstance();

    public static void main(String[] args) {
        SpringApplication.run(SampleMain.class, args);
    }

    @RequestMapping("/")
    String home() {
        StringBuilder sb = new StringBuilder("Hello Spring-Boot!!").append("\r\n").append("Update!");
        return sb.toString();
    }

    @RequestMapping("/slack_thanks")
    String getSlackThanks() {
        StringBuilder sb = new StringBuilder();

        try {
            // slackクライアント取得
            MethodsClient client = slack.methods(SLACK_TOKEN);

            // ユーザー情報取得
            Map<String, User> userMap = getUsersMap(client, SLACK_TOKEN);

            // チャンネル情報取得
            ConversationsHistoryResponse result = client.conversationsHistory(r ->
                    r.token(SLACK_TOKEN).channel(THANKS_CHANNEL)
            );

            // メッセージ用リスト作成
            Optional<List<Message>> resultList = Optional.empty();
            // Postされたメッセージ取得
            resultList = Optional.ofNullable(result.getMessages());

            // orElseなので投稿なしなら空のリストを返す。
            List<Message> newList = resultList.orElse(Collections.emptyList());
            Map<String, Integer> cntMap = new HashMap<>();
            System.out.println("***** コメント投稿日 *****");
            if(!newList.isEmpty()) {
                // 1件ずつ処理
                newList.stream().forEach(msg -> {
                    // ユーザ名取得
                    String userName = userMap.get(msg.getUser()).getName();
                    // Channel join等のメッセージ以外は判定対象外
                    if( msg.getSubtype() == null){
                        // コメント日を取得
                        String timestamp = msg.getTs().replace(".","").substring(0, 13);
                        Instant instant = Instant.ofEpochMilli(Long.parseLong(timestamp));
                        LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

                        // 結果出力
                        System.out.println("user:" + userName + " date:" + ldt.getMonthValue() +"/" + ldt.getDayOfMonth());

                        if( cntMap.get(userName) != null ) {
                            cntMap.put(userName, cntMap.get(userName) + 1);
                        } else {
                            cntMap.put(userName, 1);
                        }
                    }
                });

                //
                System.out.println("***** コメント投稿数 *****");

                // コメント数順にソート
                // 昇順はMap.Entry.comparingByValue()
                // 降順はCollections.reverseOrder(Map.Entry.comparingByValue())
                Stream<Map.Entry<String, Integer>> sortMap = cntMap.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
                sortMap.forEach((map) -> {
                    System.out.println("ユーザ名:" + map.getKey() + " 回数：" + map.getValue());
                    sb.append(map.getKey() + " / " + map.getValue());
                    sb.append("</br>");
                });
            } else {
                System.out.println("non message");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * ユーザ情報を取得しマップに格納
     * 参考：https://api.slack.com/methods/users.list/code
     * @param client
     * @param token
     * @return
     * @throws IOException
     * @throws SlackApiException
     */
    private static Map<String, User> getUsersMap(MethodsClient client, String token) throws IOException, SlackApiException {
        //
        UsersListResponse usersListResponse = client.usersList(r -> {
            return r.token(token);
        });
        List<User> userList = usersListResponse.getMembers();

        Map<String, User> userMap = userList.stream().collect(
                Collectors.toMap(User::getId, user -> {
//                    System.out.println("key:" + user.getId() + " val:" + user.getName());
                    return user;
                }));

        return userMap;
    }

}
