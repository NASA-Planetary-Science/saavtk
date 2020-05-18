package edu.jhuapl.saavtk.util;

public enum UrlStatus
{
    ACCESSIBLE, // Connection made, resource is accessible. Future access likely to succeed.
    NOT_AUTHORIZED, // Connection made, but user is not authorized.
    NOT_FOUND, // Connection made, but resource was not found.
    HTTP_ERROR, // Connection made, but HTTP error code was returned. Future access unknown.
    CONNECTION_ERROR, // Connection was not made, e.g., interruption or timeout.
    INVALID_URL, // URL itself is flawed. Future access will fail.
    UNKNOWN, // Have not succesfully obtained information about the URL.
}