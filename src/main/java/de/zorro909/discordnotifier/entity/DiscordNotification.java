package de.zorro909.discordnotifier.entity;

import de.zorro909.discordnotifier.entity.component.DiscordInteractionComponent;
import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy;
import io.micronaut.core.annotation.Introspected;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.Collection;

@Introspected
@GenerateProxy
@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscordNotification {
    @Id
    @GeneratedValue
    @Builder.Default
    private Long id = null;

    @NotNull
    private String channelName;

    @NotNull
    private String source;

    @NotNull
    private String title;

    @NotNull
    private String message;

    @Nullable
    private Long messageId;

    @Nullable
    @Column(length = 1000)
    private String imageUrl;

    @Nullable
    @Column(length = 1000)
    private String webhookUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean sent = false;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("rowIndex, columnIndex")
    private Collection<DiscordInteractionComponent> components;

    @Builder.Default
    private boolean markedForDeletion = false;

    public DiscordNotification markForDeletion() {
        this.markedForDeletion = true;
        return this;
    }

    public Long getId() {
        return id;
    }

    public boolean isSent() {
        return sent;
    }

    public boolean isMarkedForDeletion() {
        return markedForDeletion;
    }
}