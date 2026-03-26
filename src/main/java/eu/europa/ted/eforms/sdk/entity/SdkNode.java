package eu.europa.ted.eforms.sdk.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A node is something like a section. Nodes can be parents of other nodes or parents of fields.
 */
public abstract class SdkNode implements Comparable<SdkNode> {
  private final String id;
  private final String xpathAbsolute;
  private final String xpathRelative;
  private final String parentId;
  private final boolean repeatable;
  private SdkNode parent;
  private List<String> cachedAncestry;

  protected SdkNode(final String id, final String parentId, final String xpathAbsolute,
      final String xpathRelative, final boolean repeatable) {
    this.id = id;
    this.parentId = parentId;
    this.xpathAbsolute = xpathAbsolute;
    this.xpathRelative = xpathRelative;
    this.repeatable = repeatable;
  }

  protected SdkNode(JsonNode node) {
    this.id = node.get("id").asText(null);
    this.parentId = node.has("parentId") ? node.get("parentId").asText(null) : null;
    this.xpathAbsolute = node.get("xpathAbsolute").asText(null);
    this.xpathRelative = node.get("xpathRelative").asText(null);
    this.repeatable =
        node.hasNonNull("repeatable") && node.get("repeatable").asBoolean(false);
  }

  public String getId() {
    return id;
  }

  public String getParentId() {
    return parentId;
  }

  public String getXpathAbsolute() {
    return xpathAbsolute;
  }

  public String getXpathRelative() {
    return xpathRelative;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public SdkNode getParent() {
    return parent;
  }

  /**
   * Sets the parent node and invalidates the cached ancestry.
   * Should only be called during SDK initialization (two-pass loading).
   *
   * @param parent the parent node
   */
  public void setParent(SdkNode parent) {
    this.parent = parent;
    this.cachedAncestry = null;
  }

  /**
   * Returns the ancestry chain from this node to the root.
   * The list includes this node as the first element, followed by its parent,
   * grandparent, and so on up to the root node.
   *
   * The result is cached and recomputed only when the parent changes.
   *
   * @return unmodifiable list of node IDs ordered from child (this node) to root
   */
  public List<String> getAncestry() {
    if (cachedAncestry == null) {
      List<String> ancestry = new ArrayList<>();
      SdkNode current = this;
      while (current != null) {
        ancestry.add(current.getId());
        current = current.getParent();
      }
      cachedAncestry = Collections.unmodifiableList(ancestry);
    }
    return cachedAncestry;
  }

  @Override
  public int compareTo(SdkNode o) {
    return this.getId().compareTo(o.getId());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SdkNode other = (SdkNode) obj;
    return Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id;
  }
}
