package nbc.chillguys.nebulazone.domain.post.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
public class PostImage {

	private String url;

	public PostImage(String url) {
		this.url = url;
	}

}
