package org.bytedata.manager.ui.actionbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

public abstract class BaseDialogFragment<B extends ViewBinding> extends DialogFragment {

	private B binding;

	/**
	 * Override untuk DialogFragment berbasis View Binding.
	 */
	protected B getBinding() {
		return binding;
	}

	/**
	 * Override untuk DialogFragment berbasis View Binding.
	 */
	@Nullable
	protected B initializeBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
		return null;
	}

	/**
	 * Override untuk DialogFragment berbasis layout ID.
	 */
	@LayoutRes
	protected int layoutId() {
		return 0; // Default, jika subclass tidak mendefinisikan layout ID
	}

	protected <T extends View> T findViewById(@IdRes int id) {
		return requireView().findViewById(id);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		int layoutId = layoutId();
		if (layoutId != 0) {
			return inflater.inflate(layoutId, container, false);
		}

		binding = initializeBinding(inflater, container);
		if (binding != null) {
			return binding.getRoot();
		}

		throw new IllegalStateException("Either layoutId() or initializeBinding() must be overridden");
	}

	public void onBackPressed() {
		requireActivity().finish();
	}

	public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null; // Hindari memory leak
	}
}