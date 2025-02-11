package kr.megaptera.assignment.controllers;

import kr.megaptera.assignment.dtos.PostDto;
import kr.megaptera.assignment.models.MultilineText;
import kr.megaptera.assignment.models.Post;
import kr.megaptera.assignment.models.PostAuthor;
import kr.megaptera.assignment.models.PostId;
import kr.megaptera.assignment.models.PostTitle;
import kr.megaptera.assignment.repositories.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PostControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PostRepository postRepository;

    private final String POST_CONTROLLER_URL;

    public PostControllerTest(@Value("${local.server.port}") int port) {
        POST_CONTROLLER_URL = "http://localhost:" + port + "/posts";
    }

    private final Post SAMPLE_POST = new Post(
            PostId.of("0001POST"),
            PostTitle.of("제목"),
            PostAuthor.of("작성자"),
            MultilineText.of("내용")
    );


    @BeforeEach
    void setup() {
        postRepository.clear();
        postRepository.save(SAMPLE_POST);
    }

    @Test
    @DisplayName("GET /posts")
    void list() {
        List<PostDto> result = restTemplate.getForObject(POST_CONTROLLER_URL, List.class);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET /posts/{id} - with correct ID")
    void detailWithCorrectId() {
        PostDto postDto = restTemplate.getForObject(POST_CONTROLLER_URL + "/" + SAMPLE_POST.id(), PostDto.class);
        assertThat(postDto).isNotNull();
    }

    @Test
    @DisplayName("GET /posts/{id} - with incorrect ID")
    void detail() {
        PostDto postDto = restTemplate.getForObject(POST_CONTROLLER_URL + "/" + SAMPLE_POST.id() + "Incorrect", PostDto.class);
        //restTemplate 내부에서 404에 대한 처리를 리턴 클래스 생성자를 통해서 Null로 채워진 DTO를 생성해서 리턴
        assertThat(postDto.getId()).isNull();
        assertThat(postDto.getTitle()).isNull();
        assertThat(postDto.getAuthor()).isNull();
        assertThat(postDto.getContent()).isNull();
    }

    @Test
    @DisplayName("POST /posts")
    void create() {

        PostDto postDto = new PostDto("id", "title", "author", "createTestContent");

        restTemplate.postForLocation(POST_CONTROLLER_URL, postDto);

        String body = restTemplate.getForObject(POST_CONTROLLER_URL, String.class);

        assertThat(body).contains("createTestContent");
    }

    @Test
    @DisplayName("PATCH /posts/{id}")
    void update() {

        PostDto postDto = new PostDto("updatedTitle", "updatedContent");
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        PostDto updatedPost = restTemplate.patchForObject(POST_CONTROLLER_URL + "/" + SAMPLE_POST.id(), postDto, PostDto.class);

        assertThat(updatedPost.getTitle()).isEqualTo(postDto.getTitle());
        assertThat(updatedPost.getContent()).isEqualTo(postDto.getContent());
    }

    @Test
    @DisplayName("DELETE /posts/{id}")
    void deletePost() {
        PostDto postDto = new PostDto(SAMPLE_POST);

        restTemplate.delete(POST_CONTROLLER_URL + "/" + postDto.getId());

        List<PostDto> result = restTemplate.getForObject(POST_CONTROLLER_URL, List.class);

        assertThat(result.size()).isEqualTo(0);
    }
}
