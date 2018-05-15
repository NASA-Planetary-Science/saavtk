package edu.jhuapl.saavtk.gui.panel;

import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataListener;

public class JComboBoxWithItemState<E> extends JComboBox<E>
{
	private static final long serialVersionUID = -1L;

	private final Set<Object> enabledObjects;
	private Color enabledForeground;
	private Color enabledBackground;
	private Color selectedForeground;
	private Color selectedBackground;

	public JComboBoxWithItemState()
	{
		super();
		this.enabledObjects = new HashSet<>();
		this.enabledForeground = null;
		this.enabledBackground = null;
		this.selectedForeground = null;
		this.selectedBackground = null;

		// Customize behavior of default model and renderer. 
		setModel(getModel());
		setRenderer(getRenderer());
	}

	public void setEnabled(Object object, boolean enabled)
	{
		if (enabled)
		{
			enabledObjects.add(object);
		}
		else
		{
			enabledObjects.remove(object);
		}
	}

	@Override
	public void addItem(E item)
	{
		super.addItem(item);
		// Enable by default.
		setEnabled(item, true);
	}

	@Override
	public final void setModel(ComboBoxModel<E> model)
	{
		super.setModel(wrapModel(model));
	}

	@Override
	public final void setRenderer(ListCellRenderer<? super E> sourceRenderer)
	{
		if (getRenderer() != sourceRenderer)
		{
			enabledForeground = null;
			enabledBackground = null;
			selectedForeground = null;
			selectedBackground = null;
		}
		super.setRenderer(wrapRenderer(sourceRenderer));
	}

	/**
	 * This override is provided as an optimization based on a modified version of the
	 * implementation in JComboBox at the time JComboBoxWithItemState was developed.
	 * The reason for repeating the guts of the implementation here is that.
	 */
	@Override
	public void removeAllItems()
	{
		WrappedMutableComboBoxModel sourceModel = (JComboBoxWithItemState<E>.WrappedMutableComboBoxModel) getModel();

		MutableComboBoxModel<E> model = sourceModel.getWrappedModel();
		int size = model.getSize();
		if (model instanceof DefaultComboBoxModel)
		{
			((DefaultComboBoxModel<E>) model).removeAllElements();
		}
		else
		{
			for (int i = 0; i < size; ++i)
			{
				E element = sourceModel.getElementAt(0);
				sourceModel.removeElement(element);
			}
		}
		selectedItemReminder = null;
		if (isEditable())
		{
			editor.setItem(null);
		}
	}

	protected final class WrappedMutableComboBoxModel implements MutableComboBoxModel<E>
	{

		private final MutableComboBoxModel<E> mutableModel;

		protected WrappedMutableComboBoxModel(MutableComboBoxModel<E> mutableModel)
		{
			this.mutableModel = mutableModel;
		}

		protected MutableComboBoxModel<E> getWrappedModel()
		{
			return mutableModel;
		}

		@Override
		public int getSize()
		{
			return mutableModel.getSize();
		}

		@Override
		public E getElementAt(int index)
		{
			return mutableModel.getElementAt(index);
		}

		@Override
		public void addListDataListener(ListDataListener l)
		{
			mutableModel.addListDataListener(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l)
		{
			mutableModel.removeListDataListener(l);
		}

		@Override
		public void setSelectedItem(Object item)
		{
			if (!enabledObjects.contains(item))
			{
				item = null;
			}
			mutableModel.setSelectedItem(item);
		}

		@Override
		public Object getSelectedItem()
		{
			return mutableModel.getSelectedItem();
		}

		@Override
		public void addElement(E item)
		{
			mutableModel.addElement(item);
		}

		@Override
		public void removeElement(Object obj)
		{
			mutableModel.removeElement(obj);
		}

		@Override
		public void insertElementAt(E item, int index)
		{
			mutableModel.insertElementAt(item, index);
		}

		@Override
		public void removeElementAt(int index)
		{
			mutableModel.removeElementAt(index);
		}

	}

	protected final ComboBoxModel<E> wrapModel(final ComboBoxModel<E> sourceModel)
	{
		// Prevent double-wrapping. That is the reason for using a named class rather
		// than an anonymous one here.
		if (sourceModel instanceof JComboBoxWithItemState.WrappedMutableComboBoxModel)
		{
			return sourceModel;
		}

		// Only wrap mutable source models.
		if (!(sourceModel instanceof MutableComboBoxModel))
		{
			throw new RuntimeException("Cannot use this class with a non-Mutable data model.");
		}

		return new WrappedMutableComboBoxModel((MutableComboBoxModel<E>) sourceModel);
		//		return sourceModel;
	}

	protected final class WrappedListCellRenderer implements ListCellRenderer<E>
	{
		private final ListCellRenderer<? super E> sourceRenderer;

		protected WrappedListCellRenderer(ListCellRenderer<? super E> sourceRenderer)
		{
			this.sourceRenderer = sourceRenderer;

		}

		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus)
		{
			boolean enabled = enabledObjects.contains(value);
			Component component = sourceRenderer.getListCellRendererComponent(list, value, index, isSelected && enabled, cellHasFocus && enabled);
			if (isSelected)
			{
				if (selectedForeground == null)
				{
					selectedForeground = component.getForeground();
				}
				if (selectedBackground == null)
				{
					selectedBackground = component.getBackground();
				}
			}
			else
			{
				if (enabledForeground == null)
				{
					enabledForeground = component.getForeground();
				}
				if (enabledBackground == null)
				{
					enabledBackground = component.getBackground();
				}
			}
			if (enabled)
			{
				component.setForeground(isSelected ? selectedForeground : enabledForeground);
				component.setBackground(isSelected ? selectedBackground : enabledBackground);
			}
			else
			{
				component.setForeground(Color.LIGHT_GRAY);
				component.setBackground(enabledBackground);
			}
			return component;
		}

	}

	protected final ListCellRenderer<? super E> wrapRenderer(final ListCellRenderer<? super E> sourceRenderer)
	{
		if (sourceRenderer instanceof JComboBoxWithItemState.WrappedListCellRenderer)
		{
			return sourceRenderer;
		}

		return new WrappedListCellRenderer(sourceRenderer);
	}

}
