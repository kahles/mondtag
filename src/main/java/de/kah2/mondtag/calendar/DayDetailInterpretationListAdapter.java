package de.kah2.mondtag.calendar;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import de.kah2.mondtag.R;

/** {@link android.support.v7.widget.RecyclerView.Adapter} to show all
 *  {@link de.kah2.libZodiac.interpretation.Interpreter}s in {@link DayDetailFragment}.
 */
public class DayDetailInterpretationListAdapter
        extends RecyclerView.Adapter<DayDetailInterpretationListAdapter.InterpretationListItem> {

    private final LinkedList<InterpreterMapping> interpreters;

    DayDetailInterpretationListAdapter(LinkedList<InterpreterMapping> interpreters) {
        this.interpreters = interpreters;
    }

    @NonNull
    @Override
    public InterpretationListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate( R.layout.fragment_day_detail_interpretation_item,
                        parent,
                        false);

        return new InterpretationListItem(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull InterpretationListItem holder, int position) {

        final InterpreterMapping interpreter = interpreters.get(position);
        holder.bindElement(interpreter);
    }

    @Override
    public int getItemCount() {

        return interpreters.size();
    }

    class InterpretationListItem extends RecyclerView.ViewHolder {

        public InterpretationListItem(View itemView) {
            super(itemView);
        }

        void bindElement(InterpreterMapping mapping) {
            ( (ImageView) itemView.findViewById(R.id.interpretation_icon) )
                    .setImageResource( mapping.getQualityIcon() );
            ( (TextView) itemView.findViewById(R.id.interpretation_name) )
                    .setText( mapping.getId() );
            ( (TextView) itemView.findViewById(R.id.interpretation_quality_text) )
                    .setText( mapping.getQualityText() );
            ( (TextView) itemView.findViewById(R.id.interpretation_annotation_text) )
                    .setText( mapping.getAnnotations() );
        }
    }
}
