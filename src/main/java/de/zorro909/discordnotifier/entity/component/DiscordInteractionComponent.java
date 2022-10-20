package de.zorro909.discordnotifier.entity.component;

import de.zorro909.discordnotifier.api.component.ComponentType;
import de.zorro909.discordnotifier.entity.DiscordNotification;
import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@GenerateProxy
@Entity(name = "DiscordInteractionComponent")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Introspected
public class DiscordInteractionComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private DiscordNotification parent;

    @Column(nullable = false)
    private int rowIndex;

    @Column(nullable = false)
    private int columnIndex;

    @Column(nullable = false)
    private String interactionId;

    @NotNull
    @Column(nullable = false)
    private ComponentType type;

    @NotNull
    @Column(nullable = false)
    private String label;

}
