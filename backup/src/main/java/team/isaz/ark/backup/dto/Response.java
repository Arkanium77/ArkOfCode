package team.isaz.ark.backup.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import team.isaz.ark.backup.constants.Status;

@Getter
@With
@AllArgsConstructor
public class Response {
    private final Status status;
    private final String description;
    private final String id;

    public Response(Status status, String description) {
        this.status = status;
        this.description = description;
        this.id = null;
    }

    public static Response ok(String description) {
        return new Response(Status.OK, description);
    }

    public static Response error(String description) {
        return new Response(Status.ERROR, description);
    }
}
