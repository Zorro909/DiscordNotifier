package de.zorro909.discordnotifier.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscordNotification {
	@Id
	@GeneratedValue
	@Builder.Default
	private Long id = null;

	private String source;

	private String json;

	private String message;

	@Builder.Default
	private boolean sent = false;

}