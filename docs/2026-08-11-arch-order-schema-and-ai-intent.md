# 🛠️ 백엔드 Order Schema & AI 인텐트 통신 아키텍처 가이드

본 문서는 사장님 앱에서 저장한 주문서 양식(Order Schema)이 백엔드에 어떻게 저장되고, 소비자 앱에서 AI 챗봇이 이를 활용하여 주문을 접수(Intent Handling)하는지에 대한 아키텍처 및 코어 로직을 설명합니다.

## 1. 개요 (Architecture Overview)

백엔드 서버(`MakeAWish-BE`)는 프론트엔드(소비자 앱/사장님 앱)와 AI 파이썬 서버(`MakeAWish-AI`)를 이어주는 **오케스트레이터(Orchestrator)** 역할을 수행합니다.

- **흐름 요약**:
  1. 사장님 앱에서 매장 ID(`storeId`)와 함께 양식을 보내오면, 백엔드가 이를 매장 엔티티(DB)에 JSON 형태로 저장 (`StoreController.java`)
  2. 소비자가 특정 케이크를 보며 채팅창에 메시지를 보내면, 백엔드는 해당 케이크가 속한 매장의 주문서 양식(Schema JSON)을 DB에서 꺼내옴
  3. 백엔드는 "소비자의 메시지"와 "주문서 양식"을 함께 Python AI 서버(`POST /api/ai/chat`)로 전송
  4. AI 서버는 양식을 바탕으로 다음 액션을 결정(`SHOW_SCHEMA`, `CONFIRM_SLOTS` 등)하고 백엔드로 응답 반환
  5. 백엔드는 `OrderIntentHandler.java`를 통해 이 응답(Response)을 클라이언트가 소비할 수 있는 DTO 형태로 정제하여 반환

## 2. 주요 로직 및 코드 설명

### 1) AI 서버와의 통신 및 DTO 변환 (`OrderIntentHandler.java`)

AI 서버가 반환하는 액션이 **주문 관련 의도(`SHOW_SCHEMA`, `CONFIRM_SLOTS`, `ORDER_SUMMARY`)**일 경우, 이 핸들러가 동작합니다.

```java
@Component
public class OrderIntentHandler implements IntentHandler {

    @Override
    public boolean supports(AgentActionType actionType) {
        return actionType == AgentActionType.SHOW_SCHEMA ||
               actionType == AgentActionType.CONFIRM_SLOTS ||
               actionType == AgentActionType.ORDER_SUMMARY;
    }

    @Override
    public AiAgentResponse handle(User user, AiIntentResponse aiResponse) {
        // AI가 파싱해서 넘겨준 data (또는 options, extracted_slots) 객체를 그대로 클라이언트로 패스-스루 (Pass-through)
        Map<String, Object> responseData = (aiResponse.data() != null) 
                ? new java.util.HashMap<>(aiResponse.data()) 
                : new java.util.HashMap<>();
        
        // 추출된 슬롯(사용자가 입력한 맛, 사이즈 등)이 있다면 병합
        if (aiResponse.extracted_slots() != null) {
            responseData.put("extracted_slots", aiResponse.extracted_slots());
        }
        
        // 프론트엔드로 내려보낼 최종 DTO 완성
        return new AiAgentResponse(
            aiResponse.actionType().name(),
            aiResponse.message(),
            responseData, // 여기에 AI 서버가 생성한 'options' 배열도 포함됩니다.
            null
        );
    }
}
```

### 2) 왜 이런 아키텍처인가? (Design Philosophy)

백엔드는 주문 상태나 양식의 복잡한 로직을 직접 파싱하지 않습니다.
대신 **"데이터 라우터"**로서의 역할에 집중합니다. 
AI 서버가 만든 `options`나 프론트엔드가 요구하는 형식의 제약 없이, AI 서버가 반환한 `data` Map을 그대로 `AiAgentResponse`에 실어 프론트엔드에 넘깁니다. 
이렇게 하면 추후 양식 모델이 변경되거나 AI 프롬프트가 변경되어도 백엔드 코드를 재배포할 필요가 없습니다! (Loose Coupling)
