/*
 * ome.util.mem.Handle
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

/**
 * Provides the basic machinery to share the same logical state across objects
 * with different identities.
 * <p>
 * This class calls for a distinction between object identity and object state.
 * Made this distinction, it becomes possible to share the same state across
 * different objects upon copy operations. When an object is updated, a new
 * state representation is bound to that object, while the other objects can
 * still share the previous state. This way we can have shallow copy with the
 * semantics of deep copy. This can result in dramatically reduced memory
 * footprint when:
 * </p>
 * <ul>
 * <li>A considerable amount of copies of a given master object are needed.</li>
 * <li>You can't use references to the master object, because the copied
 * objects need to have their own identity.</li>
 * <li>The number of copied objects that are going to change their state after
 * the copy operation is small compared to the total number of copies.</li>
 * </ul>
 * <p>
 * For example, think of a class <code>R</code> that represents a rectangle in
 * the plane with <code>4</code> integer fields <code>x, y, w, h</code>,
 * and say you want to use instances of this class to describe an ROI (region of
 * interest) selection in a given image 3D-stack composed of <code>100</code>
 * planes &#151; each plane would contain a rectangle selection and all those
 * selections would make up your ROI. Let's assume that the initial selection is
 * a discrete 3D-rectangle that spans all planes in the stack &#151; you would
 * have one rectangle per plane, every rectangle would have exactly the same
 * state, say <code>s[x=0, y=0, w=3, h=4]</code>. Moreover, let's assume that
 * you will have to modify slightly this initial ROI in order to get the final
 * selection &#151; for example by resizing/moving a couple of rectangles within
 * the selection. Now, when you start off with the initial selection, you could
 * decide to clone an initial master object whose state is <code>s</code>
 * &#151; this way, you can later modify one of the copies without affecting the
 * others. However, because <i>Java</i> makes no distinction between object
 * identity and state, you would have in memory <code>100</code> references
 * and <code>100</code> copies of the same logical state <code>s</code>,
 * while you actually only need one.
 * <p>
 * <p>
 * The purpose of this class is to help you save memory in situations like that
 * just described by approximating the semantics of the well known Handle/Body
 * and Counted Body idioms often found in <i>C++</i> programs. A given class
 * abstraction is implemented by two actual classes which replicate the same
 * class interface. One class, the Handle, takes on the role of an object
 * identifier and forwards all calls to the other class, the Body, which
 * implements the actual functionality. Clients can only access instances of the
 * Handle which can all share the same Body object whenever appropriate.
 * </p>
 * <p>
 * The way this works in our case is pretty easy. An Handle class extends this
 * base <code>Handle</code> class and provides a reference to an instance of
 * the corresponding Body class. The concrete Handle class exposes the same
 * interface as its corresponding Body (this is not an absolute requirement, but
 * usually an implementation trade-off) and has <i>no state</i> &#151; in fact,
 * the state is hold by the associated Body object. The Handle just forwards to
 * the Body any call that only reads the Body's state. However, it must call the
 * {@link #breakSharing() breakSharing} protected method <i>before</i>
 * forwarding any call that modifies the Body's state. It is crucial that
 * concrete <code>Handle</code> classes stick to this rule. In fact, the
 * {@link #copy() copy} method simply rebinds a new <code>Handle</code> to the
 * existing Body, so subclasses must notify any incumbent change to the Body's
 * state for the <code>Handle</code> to break state sharing. Lastly, it's also
 * fundamental that the Body class implements the {@link Copiable} interface
 * correctly for all this to work properly.
 * </p>
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/09 15:01:57 $) </small>
 * @since OME2.2
 */
public abstract class Handle implements Copiable, Cloneable {

    /** Reference to the Body object. */
    private Copiable body;

    /**
     * Tells whether the {@link #body} is referenced by other
     * <code>Handle</code> objects.
     */
    private boolean shared;

    /**
     * Subclasses use this constructor to specify the Body instance this handle
     * will be paired up with. Subclasses must pass in a <i>newly</i> created
     * object.
     * 
     * @param body
     *            Reference to the Body object. Mustn't be <code>null</code>.
     */
    protected Handle(Copiable body) {
        if (body == null) {
            throw new NullPointerException("No body.");
        }
        this.body = body;
        shared = false;
    }

    /**
     * Returns a reference to the Body object that is <i>currently</i> paired
     * up with this handle. The type of the returned object is the same as the
     * one of the object that was passed to this class' protected constructor.
     * However, the object returned by this method could be different from the
     * one initially passed in at creation time if the
     * {@link #breakSharing() breakSharing} method has been invoked. For this
     * reason, subclasses mustn't cache a reference to the object returned by
     * this method. Moreover, subclasses must never leak out a reference to the
     * returned Body object.
     * 
     * @return The Body object.
     */
    protected Object getBody() {
        return body;
    }

    /**
     * Subclasses must call this method <i>before</i> forwarding any call that
     * modifies the Body's state.
     */
    protected final void breakSharing() {
        if (shared) {
            body = (Copiable) body.copy();
            shared = false;
        }
    }

    /**
     * Returns a deep copy of this object. To be precise, this method returns an
     * object that will behave like a deep copy, but has a negligible memory
     * footprint until an attempt to change its state is made. Then the whole
     * original state is restored in memory so that the state change operation
     * can take place.
     * 
     * @return A deep copy of this object. The class of the returned object is
     *         the same as the class of this object.
     */
    public final Object copy() {
        Handle h;

        // Make a shallow copy of this object. This is fine b/c subclasses
        // are not supposed to hold any state, never mind references to other
        // objects :)
        try {
            h = (Handle) clone(); // Class of h is this instance's class.
        } catch (CloneNotSupportedException cnse) {
            // Shouldn't happen as this class implements Cloneable.
            throw new InternalError(
                    "JVM Internal Error: couldn't clone object that "
                            + "implements Cloneable.");
        }
        h.body = this.body; // Not actually needed, added for clarity.

        // Set state sharing flag.
        h.shared = true;
        this.shared = true;

        return h;
    }

}
