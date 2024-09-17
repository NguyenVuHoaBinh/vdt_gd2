package Viettel.backend.service.chat;

import Viettel.backend.model.ChatDocument;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatQueryService {

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ChatQueryService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public List<ChatDocument> searchChatsByText(String queryText) throws IOException {
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index("chat_index")
                .query(q -> q
                        .match(m -> m
                                .field("message")
                                .query(queryText)
                        )
                )
        );

        SearchResponse<ChatDocument> searchResponse = elasticsearchClient.search(searchRequest, ChatDocument.class);

        return searchResponse.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}
