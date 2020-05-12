package com.onarandombox.multiverseinventories.migration.vanilla;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * NBT IO Class
 *
 * @see <a href="https://minecraft.gamepedia.com/NBT_format">NBT format</a>
 * @see <a href="https://minecraft.gamepedia.com/Development_resources/Example_NBT_Class">Example NBT Class</a>
 */
public class Tag {
    private final Type type;
    private final String name;
    private final Object value;
    private Type listType = null;

    /**
     * Enum for the tag types.
     */
    public enum Type {
        TAG_End,
        TAG_Byte,
        TAG_Short,
        TAG_Int,
        TAG_Long,
        TAG_Float,
        TAG_Double,
        TAG_Byte_Array,
        TAG_String,
        TAG_List,
        TAG_Compound,
        TAG_Int_Array,
        TAG_Long_Array
    }

    /**
     * Create a new TAG_List or TAG_Compound NBT tag.
     *
     * @param type either TAG_List or TAG_Compound
     * @param name name for the new tag or null to create an unnamed tag.
     * @param value list of tags to add to the new tag.
     */
    public Tag(Type type, String name, Tag[] value) {
        this(type, name, (Object) value);
    }

    /**
     * Create a new NBT tag.
     *
     * @param type any value from the {@link Type} enum.
     * @param name name for the new tag or null to create an unnamed tag.
     * @param value an object that fits the tag type or a {@link Type} to create an empty TAG_List with this list type.
     */
    public Tag(Type type, String name, Object value) {
        switch (type) {
            case TAG_End:
                if (value != null) throw new IllegalArgumentException();
                break;
            case TAG_Byte:
                if (!(value instanceof Byte)) throw new IllegalArgumentException();
                break;
            case TAG_Short:
                if (!(value instanceof Short)) throw new IllegalArgumentException();
                break;
            case TAG_Int:
                if (!(value instanceof Integer)) throw new IllegalArgumentException();
                break;
            case TAG_Long:
                if (!(value instanceof Long)) throw new IllegalArgumentException();
                break;
            case TAG_Float:
                if (!(value instanceof Float)) throw new IllegalArgumentException();
                break;
            case TAG_Double:
                if (!(value instanceof Double)) throw new IllegalArgumentException();
                break;
            case TAG_Byte_Array:
                if (!(value instanceof byte[])) throw new IllegalArgumentException();
                break;
            case TAG_String:
                if (!(value instanceof String)) throw new IllegalArgumentException();
                break;
            case TAG_List:
                if (value instanceof Type) {
                    this.listType = (Type) value;
                    value = new Tag[0];
                } else {
                    if (!(value instanceof Tag[])) throw new IllegalArgumentException();
                    this.listType = (((Tag[]) value)[0]).getType();
                }
                break;
            case TAG_Compound:
                if (!(value instanceof Tag[])) throw new IllegalArgumentException();
                break;
            case TAG_Int_Array:
                if (!(value instanceof int[])) throw new IllegalArgumentException();
                break;
            case TAG_Long_Array:
                if (!(value instanceof long[])) throw new IllegalArgumentException();
                break;
            default:
                throw new IllegalArgumentException();
        }

        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Type getListType() {
        return listType;
    }

    /**
     * Find the first nested tag with specified name in a TAG_Compound.
     *
     * @param name the name to look for. May be null to look for unnamed tags.
     * @return the first nested tag that has the specified name.
     */
    public @Nullable Tag findTagByName(String name) {
        return findNextTagByName(name, null);
    }

    /**
     * Find the first nested tag with specified name in a TAG_List or TAG_Compound after a tag with the same name.
     *
     * @param name the name to look for. May be null to look for unnamed tags.
     * @param found the previously found tag with the same name.
     * @return the first nested tag that has the specified name after the previously found tag.
     */
    public @Nullable Tag findNextTagByName(String name, Tag found) {
        if (type != Type.TAG_List && type != Type.TAG_Compound) return null;
        Tag[] subtags = (Tag[]) value;
        for (Tag subtag : subtags) {
            if ((subtag.name == null && name == null) || (subtag.name != null && subtag.name.equals(name))) return subtag;
            else {
                Tag newFound = subtag.findTagByName(name);
                if (newFound != null && newFound != found) return newFound;
            }
        }

        return null;
    }

    /**
     * Read a tag and its nested tags from an InputStream.
     *
     * @param is stream to read from, like a FileInputStream
     * @return NBT tag or structure read from the InputStream
     * @throws IOException if there was no valid NBT structure in the InputStream or if another IOException occurred.
     */
    public static @NotNull Tag readFrom(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(new GZIPInputStream(is));
        byte type = dis.readByte();
        Tag tag;

        if (type == 0) {
            tag = new Tag(Type.TAG_End, null, null);
        } else {
            tag = new Tag(Type.values()[type], dis.readUTF(), readPayload(dis, type));
        }

        dis.close();

        return tag;
    }

    private static @Nullable Object readPayload(DataInputStream dis, byte type) throws IOException {
        switch (type) {
            case 0:
                return null;
            case 1:
                return dis.readByte();
            case 2:
                return dis.readShort();
            case 3:
                return dis.readInt();
            case 4:
                return dis.readLong();
            case 5:
                return dis.readFloat();
            case 6:
                return dis.readDouble();
            case 7:
                int length = dis.readInt();
                byte[] ba = new byte[length];
                dis.readFully(ba);
                return ba;
            case 8:
                return dis.readUTF();
            case 9:
                byte lt = dis.readByte();
                int ll = dis.readInt();
                Tag[] lo = new Tag[ll];
                for (int i = 0; i < ll; i++) lo[i] = new Tag(Type.values()[lt], null, readPayload(dis, lt));
                if (lo.length == 0) return Type.values()[lt];
                else return lo;
            case 10:
                byte stt;
                Tag[] tags = new Tag[0];
                do {
                    stt = dis.readByte();
                    String name = null;
                    if (stt != 0) name = dis.readUTF();
                    Tag[] newTags = new Tag[tags.length + 1];
                    System.arraycopy(tags, 0, newTags, 0, tags.length);
                    newTags[tags.length] = new Tag(Type.values()[stt], name, readPayload(dis, stt));
                    tags = newTags;
                } while (stt != 0);
                return tags;
            case 11:
                int intLen = dis.readInt();
                int[] ia = new int[intLen];
                for (int i = 0; i < intLen; i++) ia[i] = dis.readInt();
                return ia;
            case 12:
                int longLen = dis.readInt();
                long[] la = new long[longLen];
                for (int i = 0; i < longLen; i++) la[i] = dis.readLong();
                return la;
        }

        return null;
    }
}