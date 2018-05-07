package edu.jhuapl.saavtk.gui.panel;

import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
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

	protected final ComboBoxModel<E> wrapModel(final ComboBoxModel<E> sourceModel)
	{
		return new MutableComboBoxModel<E>() {
			@Override
			public int getSize()
			{
				return sourceModel.getSize();
			}

			@Override
			public E getElementAt(int index)
			{
				return sourceModel.getElementAt(index);
			}

			@Override
			public void addListDataListener(ListDataListener l)
			{
				sourceModel.addListDataListener(l);
			}

			@Override
			public void removeListDataListener(ListDataListener l)
			{
				sourceModel.removeListDataListener(l);
			}

			@Override
			public void setSelectedItem(Object item)
			{
				if (enabledObjects.contains(item))
				{
					sourceModel.setSelectedItem(item);
				}
			}

			@Override
			public Object getSelectedItem()
			{
				return sourceModel.getSelectedItem();
			}

			@Override
			public void addElement(E item)
			{
				toMutableComboBoxModel().addElement(item);
			}

			@Override
			public void removeElement(Object obj)
			{
				toMutableComboBoxModel().removeElement(obj);
			}

			@Override
			public void insertElementAt(E item, int index)
			{
				toMutableComboBoxModel().insertElementAt(item, index);
			}

			@Override
			public void removeElementAt(int index)
			{
				toMutableComboBoxModel().removeElementAt(index);
			}

			private MutableComboBoxModel<E> toMutableComboBoxModel()
			{
				if (sourceModel instanceof MutableComboBoxModel)
				{
					return (MutableComboBoxModel<E>) sourceModel;
				}
				throw new RuntimeException("Cannot use this method with a non-Mutable data model.");
			}

		};
	}

	protected final ListCellRenderer<? super E> wrapRenderer(final ListCellRenderer<? super E> sourceRenderer)
	{
		return (list, value, index, isSelected, cellHasFocus) -> {
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
		};
	}

}
