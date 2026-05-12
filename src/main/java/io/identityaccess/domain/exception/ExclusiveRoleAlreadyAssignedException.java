package io.identityaccess.domain.exception;

public class ExclusiveRoleAlreadyAssignedException extends DomainException {

    public ExclusiveRoleAlreadyAssignedException() {
        super("rol_exclusivo_ya_asignado", "Exclusive role already has an active assignment");
    }
}
