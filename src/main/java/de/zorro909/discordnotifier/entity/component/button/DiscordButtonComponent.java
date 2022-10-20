package de.zorro909.discordnotifier.entity.component.button;

import de.zorro909.discordnotifier.api.component.button.ButtonStyle;
import de.zorro909.discordnotifier.entity.component.DiscordInteractionComponent;
import de.zorro909.discordnotifier.entity.component.text.DiscordTextInputComponent;
import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy;
import io.micronaut.core.annotation.Introspected;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.List;

@GenerateProxy
@Entity(name = "DiscordButtonComponent")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Introspected
public class DiscordButtonComponent extends DiscordInteractionComponent {

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private ButtonStyle buttonStyle = ButtonStyle.PRIMARY;

    @Nullable
    @Column(length = 100)
    @Builder.Default
    private String linkUrl = null;

    @Nullable
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<DiscordTextInputComponent> modalTextInputs;

}
