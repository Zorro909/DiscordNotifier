package de.zorro909.discordnotifier.entity.component.select;

import de.zorro909.discordnotifier.entity.component.DiscordInteractionComponent;
import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.List;

@GenerateProxy
@Entity(name = "DiscordSelectMenuComponent")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@Introspected
public class DiscordSelectMenuComponent extends DiscordInteractionComponent {

    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("orderIndex ASC")
    private List<SelectOption> options;

}
