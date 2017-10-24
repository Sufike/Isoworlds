package sponge.Commandes.SousCommandes;

/**
 * Created by Edwin on 14/10/2017.
 */

import common.Messages;
import sponge.IworldsSponge;
import sponge.Utils.IworldsUtils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

public class ConfianceCommande implements CommandCallable {

    static final String SELECT = "SELECT * FROM `iworlds` WHERE `UUID_P` = ? AND `UUID_W` = ?";
    static final String INSERT = "INSERT INTO `autorisations` (`UUID_P`, `UUID_W`, `DATE_TIME`) VALUES (?, ?, ?)";
    static final String CHECK = "SELECT * FROM `autorisations` WHERE `UUID_P` = ? AND `UUID_W` = ?";

    private final IworldsSponge plugin = IworldsSponge.instance;

    @Override
    public CommandResult process(CommandSource source, String args) throws CommandException {

        // SQL Variables
        final String Suuid_p;
        final String Suuid_w;
        final String Iuuid_p;
        final String Iuuid_w;
        final String check_w;
        final String check_p;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        UUID uuidcible;
        Player pPlayer = (Player) source;
        String[] arg = args.split(" ");
        int size = arg.length;

        try {
            UserStorageService userStorage = Sponge.getServiceManager().provide(UserStorageService.class).get();
            Optional<User> player = userStorage.get(arg[0]);
            uuidcible = player.get().getUniqueId();
            if (uuidcible.toString().isEmpty() || (size > 1)) {
                IworldsUtils.coloredMessage(pPlayer, Messages.getMessage("INVALIDE_JOUEUR"));
                return CommandResult.success();
            }
        } catch (NoSuchElementException | IllegalArgumentException i) {
            i.printStackTrace();
            IworldsUtils.coloredMessage(pPlayer, Messages.getError("SQL"));
            return CommandResult.success();
        }

        try {
            // CHECK AUTORISATIONS
            try {
                PreparedStatement check = plugin.database.prepare(this.CHECK);
                // UUID _P
                check_p = uuidcible.toString();
                check.setString(1, check_p);
                // UUID_W
                check_w = (IworldsUtils.PlayerToUUID(pPlayer) + "-iWorld");
                check.setString(2, check_w);
                // Requête
                ResultSet rselect = check.executeQuery();
                if (rselect.isBeforeFirst() ) {
                    pPlayer.sendMessage(Text.of(Text.builder("[iWorlds]: ").color(TextColors.GOLD)
                            .append(Text.of(Text.builder(Messages.getMessage("EXISTE_TRUST")).color(TextColors.AQUA))).build()));
                    return CommandResult.success();
                }
            } catch (Exception se) {
                se.printStackTrace();
                pPlayer.sendMessage(Text.of(Text.builder("[iWorlds]: ").color(TextColors.GOLD)
                        .append(Text.of(Text.builder(Messages.getError("SQL")).color(TextColors.AQUA))).build()));
                return CommandResult.success();
            }

            // SELECT WORLD
            try {
                PreparedStatement select = plugin.database.prepare(this.SELECT);
                // UUID_P
                Suuid_p = pPlayer.getUniqueId().toString();
                select.setString(1, Suuid_p);
                // UUID_W
                Suuid_w = (IworldsUtils.PlayerToUUID(pPlayer) + "-iWorld");
                select.setString(2, Suuid_w);
                // Requête
                ResultSet rselect = select.executeQuery();
                if (!rselect.isBeforeFirst() ) {
                    pPlayer.sendMessage(Text.of(Text.builder("[iWorlds]: ").color(TextColors.GOLD)
                            .append(Text.of(Text.builder(Messages.getMessage("EXISTE_PAS_IWORLD")).color(TextColors.AQUA))).build()));
                    return CommandResult.success();
                }
            } catch (Exception se) {
                se.printStackTrace();
                pPlayer.sendMessage(Text.of(Text.builder("[iWorlds]: ").color(TextColors.GOLD)
                        .append(Text.of(Text.builder(Messages.getError("SQL")).color(TextColors.AQUA))).build()));
                return CommandResult.success();
            }

            // INSERT
            try {
                PreparedStatement insert = plugin.database.prepare(this.INSERT);
                // UUID_P
                Iuuid_p = uuidcible.toString();
                insert.setString(1, Iuuid_p);
                // UUID_W
                Iuuid_w = (IworldsUtils.PlayerToUUID(pPlayer) + "-iWorld");
                insert.setString(2, Iuuid_w);
                // Date
                insert.setString(3, (timestamp.toString()));
                insert.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                return CommandResult.success();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            pPlayer.sendMessage(Text.of(Text.builder("[iWorlds]: ").color(TextColors.GOLD)
                    .append(Text.of(Text.builder(Messages.getError("SQL")).color(TextColors.AQUA))).build()));
            return CommandResult.success();
        }

        pPlayer.sendMessage(Text.of(Text.builder("[iWorlds]: ").color(TextColors.GOLD)
                .append(Text.of(Text.builder(Messages.getMessage("SUCCES_TRUST")).color(TextColors.AQUA))).build()));
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
        return null;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return false;
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return null;
    }

    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return null;
    }

    @Override
    public Text getUsage(CommandSource source) {
        return null;
    }
}