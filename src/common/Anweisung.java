package common;
/**
 * Available instructions of the protocol.
 */
public enum Anweisung {
    /**CONNECT
     *connection request
     *usage: CON
     */
    CON,
    /**DISCONNECT
     *disconnect notification
     *usage: DSC
     */
    DSC,
    /**ACKNOWLEDGED
     *operation acknowledgement
     *usage: ACK
     */
    ACK,
    /**DENIED
     *negative operation acknowledgement
     *usage: DND
     */
    DND,
    /**LIST
     *list a directory
     *usage: LST
     */
    LST,
    /**UPLOAD
     *upload a file
     *usage: PUT <filename : string>
     */
    PUT,
    /**DOWNLOAD
     *download a file
     *usage: GET <filename : string>
     */
    GET,
    /**DELETE
     *delete a file
     *usage: DEL <filename : string>
     */
    DEL,
    /**DATA
     *encapsulates the data to be transmitted
     *usage: DAT <length : string (long)> <data : byte[]>
     */
    DAT,
}