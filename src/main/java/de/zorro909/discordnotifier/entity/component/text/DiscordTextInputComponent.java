package de.zorro909.discordnotifier.entity.component.text;

import de.zorro909.discordnotifier.entity.component.button.DiscordButtonComponent;
import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy;
import io.micronaut.core.annotation.Introspected;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;

@GenerateProxy
@Entity(name = "DiscordTextInputComponent")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Introspected
public class DiscordTextInputComponent {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private int orderIndex;

    @ManyToOne
    private DiscordButtonComponent parent;

    @NotNull
    private String interactionId;

    @NotNull
    private String label;

    @Nullable
    @Column(nullable = true, length = 100)
    private String placeholder;

    @Column(nullable = false)
    @Builder.Default
    private int minLength = 0;

    @Column(nullable = false)
    @Builder.Default
    private int maxLength = 4000;

    @Column(nullable = false)
    @Builder.Default
    private boolean required = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean multiline = false;

}
